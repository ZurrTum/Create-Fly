package com.zurrtum.create.client.content.logistics.stockTicker;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.stockTicker.StockKeeperCategoryMenu;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.packet.c2s.GhostItemSubmitPacket;
import com.zurrtum.create.infrastructure.packet.c2s.StockKeeperCategoryEditPacket;
import com.zurrtum.create.infrastructure.packet.c2s.StockKeeperCategoryRefundPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StockKeeperCategoryScreen extends AbstractSimiContainerScreen<StockKeeperCategoryMenu> {

    private static final int CARD_HEADER = 20;
    private static final int CARD_WIDTH = 160;

    private List<Rect2i> extraAreas = Collections.emptyList();

    private final LerpedFloat scroll = LerpedFloat.linear().startWithValue(0);

    private final List<ItemStack> schedule;
    private IconButton confirmButton;
    private ItemStack editingItem;
    private int editingIndex;
    private IconButton editorConfirm;
    private TextFieldWidget editorEditBox;
    private ElementWidget renderedItem;

    final int slices = 4;

    public StockKeeperCategoryScreen(StockKeeperCategoryMenu menu, PlayerInventory inv, Text title) {
        super(menu, inv, title);
        schedule = new ArrayList<>(menu.contentHolder.categories);
        menu.slotsActive = false;
    }

    public static StockKeeperCategoryScreen create(
        MinecraftClient mc,
        MenuType<StockTickerBlockEntity> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        return type.create(StockKeeperCategoryScreen::new, syncId, inventory, title, getBlockEntity(mc, extraData));
    }

    @Override
    protected void init() {
        AllGuiTextures bg = AllGuiTextures.STOCK_KEEPER_CATEGORY;
        setWindowSize(
            bg.getWidth(),
            bg.getHeight() * slices + AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight() + AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.getHeight()
        );
        super.init();
        clearChildren();

        confirmButton = new IconButton(x + bg.getWidth() - 25, y + backgroundHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> client.player.closeHandledScreen());
        addDrawableChild(confirmButton);

        stopEditing();

        extraAreas = ImmutableList.of(new Rect2i(x + bg.getWidth(), y + backgroundHeight - 40, 48, 40));

        renderedItem = new ElementWidget(x + AllGuiTextures.STOCK_KEEPER_CATEGORY.getWidth() + 12, y + backgroundHeight - 39).showingElement(
            GuiGameElement.of(AllItems.STOCK_TICKER.getDefaultStack()).scale(3));
        addDrawableChild(renderedItem);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
    }

    protected void startEditing(int index) {
        confirmButton.visible = false;

        editorConfirm = new IconButton(x + 36 + 131, y + 59, AllIcons.I_CONFIRM);
        handler.slotsActive = true;

        editorEditBox = new TextFieldWidget(textRenderer, x + 47, y + 28, 124, 10, Text.empty());
        editorEditBox.setEditableColor(0xffeeeeee);
        editorEditBox.setDrawsBackground(false);
        editorEditBox.setFocused(false);
        editorEditBox.mouseClicked(0, 0, 0);
        editorEditBox.setMaxLength(28);
        editorEditBox.setText(index == -1 || schedule.get(index).isEmpty() ? CreateLang.translate("gui.stock_ticker.new_category")
            .string() : schedule.get(index).getName().getString());

        editingIndex = index;
        editingItem = index == -1 ? ItemStack.EMPTY : schedule.get(index);
        handler.proxyInventory.setStack(0, editingItem);
        client.player.networkHandler.sendPacket(new GhostItemSubmitPacket(editingItem, 0));

        addDrawableChild(editorConfirm);
        addDrawableChild(editorEditBox);
        backgroundHeight = 88 + AllGuiTextures.PLAYER_INVENTORY.getHeight();
    }

    protected void stopEditing() {
        confirmButton.visible = true;
        if (editingItem == null)
            return;

        playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1);
        remove(editorConfirm);
        remove(editorEditBox);

        ItemStack stackInSlot = handler.proxyInventory.getStack(0).copy();
        boolean empty = stackInSlot.isEmpty();

        if (empty && editingIndex != -1)
            schedule.remove(editingIndex);

        if (!empty) {
            String value = editorEditBox.getText();
            boolean blank = value.isBlank() || value.equals(CreateLang.translate("gui.stock_ticker.new_category").string()) || value.equals(
                stackInSlot.getName().getString());
            stackInSlot.set(DataComponentTypes.CUSTOM_NAME, blank ? null : Text.literal(value));
            if (editingIndex == -1)
                schedule.add(stackInSlot);
            else
                schedule.set(editingIndex, stackInSlot);
        }

        client.player.networkHandler.sendPacket(new GhostItemSubmitPacket(ItemStack.EMPTY, 0));

        editingItem = null;
        editorConfirm = null;
        editorEditBox = null;
        handler.slotsActive = false;
        renderedItem.getRenderElement().clear();
        init();
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        scroll.tickChaser();
        if (editorEditBox == null)
            return;
        if (!editorEditBox.getText().equals(CreateLang.translate("gui.stock_ticker.new_category").string()))
            return;
        if (handler.proxyInventory.getStack(0).contains(DataComponentTypes.CUSTOM_NAME))
            editorEditBox.setText(handler.proxyInventory.getStack(0).getName().getString());
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        partialTicks = AnimationTickHolder.getPartialTicksUI(client.getRenderTickCounter());

        if (handler.slotsActive)
            super.render(graphics, mouseX, mouseY, partialTicks);
        else {
            for (Drawable widget : drawables)
                widget.render(graphics, mouseX, mouseY, partialTicks);
            renderForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    protected void renderCategories(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        Matrix3x2fStack matrixStack = graphics.getMatrices();
        int yOffset = 25;
        List<ItemStack> entries = schedule;
        float scrollOffset = -scroll.getValue(partialTicks);

        graphics.enableScissor(x + 3, y + 16, x + 187, y + 19 + (AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight() * slices));
        for (int i = 0; i <= entries.size(); i++) {
            matrixStack.pushMatrix();
            matrixStack.translate(0, scrollOffset);

            if (i == entries.size()) {
                AllGuiTextures.STOCK_KEEPER_CATEGORY_NEW.render(graphics, x + 7, y + yOffset);
                matrixStack.popMatrix();
                break;
            }

            ItemStack scheduleEntry = entries.get(i);
            int cardY = yOffset;
            int cardHeight = renderScheduleEntry(graphics, matrixStack, i, scheduleEntry, cardY);
            yOffset += cardHeight;

            matrixStack.popMatrix();
        }
        graphics.disableScissor();
    }

    public int renderScheduleEntry(DrawContext graphics, Matrix3x2fStack matrixStack, int i, ItemStack entry, int yOffset) {
        int cardWidth = CARD_WIDTH;
        int cardHeader = CARD_HEADER;

        matrixStack.pushMatrix();
        matrixStack.translate(x + 7, y + yOffset);

        AllGuiTextures.STOCK_KEEPER_CATEGORY_ENTRY.render(graphics, 0, 0);

        if (i > 0)
            AllGuiTextures.STOCK_KEEPER_CATEGORY_UP.render(graphics, cardWidth + 12, cardHeader - 18);
        if (i < schedule.size() - 1)
            AllGuiTextures.STOCK_KEEPER_CATEGORY_DOWN.render(graphics, cardWidth + 12, cardHeader - 9);

        graphics.drawItem(entry, 14, 1);
        Text name = entry.getName();
        graphics.drawText(
            textRenderer,
            entry.isEmpty() ? CreateLang.translate("gui.stock_ticker.empty_category_name_placeholder").string() : name.asTruncatedString(20)
                .stripTrailing() + (name.getString().length() > 20 ? "..." : ""),
            35,
            5,
            0xFF656565,
            false
        );

        matrixStack.popMatrix();
        return cardHeader;
    }

    private final Text clickToEdit = CreateLang.translateDirect("gui.schedule.lmb_edit").formatted(Formatting.DARK_GRAY, Formatting.ITALIC);

    public boolean action(@Nullable DrawContext graphics, double mouseX, double mouseY, int click) {
        // Prevent actions outside the window for them
        if (mouseX < this.x || mouseX >= this.x + backgroundWidth || mouseY < this.y + 15 || mouseY >= this.y + 99)
            return false;

        if (editingItem != null)
            return false;

        int mx = (int) mouseX;
        int my = (int) mouseY;
        int x = mx - this.x - 20;
        int y = my - this.y - 24;
        if (x < 0 || x >= 196)
            return false;
        if (y < 0 || y >= 143)
            return false;
        y += scroll.getValue(0);

        List<ItemStack> entries = schedule;
        for (int i = 0; i < entries.size(); i++) {
            ItemStack entry = entries.get(i);
            int cardHeight = CARD_HEADER;

            if (y >= cardHeight) {
                y -= cardHeight;
                if (y < 0)
                    return false;
                continue;
            }

            int fieldSize = 140;
            if (x > 0 && x <= fieldSize && y > 0 && y <= 16) {
                List<Text> components = new ArrayList<>();
                components.add(entry.isEmpty() ? CreateLang.translate("gui.stock_ticker.empty_category_name_placeholder")
                    .component() : entry.getName());
                components.add(clickToEdit);
                renderActionTooltip(graphics, components, mx, my);
                if (click == 0)
                    startEditing(i);
                return true;
            }

            if (x > fieldSize && x <= fieldSize + 16 && y > 0 && y <= 16) {
                renderActionTooltip(graphics, ImmutableList.of(CreateLang.translate("gui.stock_ticker.delete_category").component()), mx, my);
                if (click == 0) {
                    if (!entry.isEmpty())
                        client.player.networkHandler.sendPacket(new StockKeeperCategoryRefundPacket(handler.contentHolder.getPos(), entry));
                    entries.remove(entry);
                    renderedItem.getRenderElement().clear();
                    init();
                }
                return true;
            }

            if (x > 158 && x < 170) {
                if (y > 2 && y <= 10 && i > 0) {
                    renderActionTooltip(
                        graphics, ImmutableList.of(
                            CreateLang.translateDirect("gui.schedule.move_up"),
                            CreateLang.translate("gui.stock_ticker.shift_moves_top").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
                        ), mx, my
                    );
                    if (click == 0) {
                        entries.remove(entry);
                        entries.add(hasShiftDown() ? 0 : i - 1, entry);
                        renderedItem.getRenderElement().clear();
                        init();
                    }
                    return true;
                }
                if (y > 10 && y <= 22 && i < entries.size() - 1) {
                    renderActionTooltip(
                        graphics, ImmutableList.of(
                            CreateLang.translateDirect("gui.schedule.move_down"),
                            CreateLang.translate("gui.stock_ticker.shift_moves_bottom").style(Formatting.DARK_GRAY).style(Formatting.ITALIC)
                                .component()
                        ), mx, my
                    );
                    if (click == 0) {
                        entries.remove(entry);
                        entries.add(hasShiftDown() ? entries.size() : i + 1, entry);
                        renderedItem.getRenderElement().clear();
                        init();
                    }
                    return true;
                }
            }

            x -= 18;
            y -= 28;

            if (x < 0 || y < 0 || x > 160)
                return false;
        }

        if (x > 0 && x <= 16 && y > 0 && y <= 16) {
            renderActionTooltip(graphics, ImmutableList.of(CreateLang.translate("gui.stock_ticker.new_category").component()), mx, my);
            if (click == 0) {
                playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                startEditing(-1);
            }
        }

        return false;
    }

    private void renderActionTooltip(@Nullable DrawContext graphics, List<Text> tooltip, int mx, int my) {
        if (graphics != null)
            graphics.drawTooltip(textRenderer, tooltip, Optional.empty(), mx, my);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (editorConfirm != null && editorConfirm.isMouseOver(pMouseX, pMouseY)) {
            stopEditing();
            return true;
        }
        if (action(null, pMouseX, pMouseY, pButton)) {
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
            return true;
        }

        boolean wasNotFocused = editorEditBox != null && !editorEditBox.isFocused();
        boolean mouseClicked = super.mouseClicked(pMouseX, pMouseY, pButton);

        if (editorEditBox != null && editorEditBox.isMouseOver(pMouseX, pMouseY) && wasNotFocused) {
            editorEditBox.setCursorToEnd(false);
            editorEditBox.setSelectionEnd(0);
        }

        return mouseClicked;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (editingItem == null)
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);

        boolean hitEscape = pKeyCode == GLFW.GLFW_KEY_ESCAPE;
        boolean hitEnter = getFocused() instanceof TextFieldWidget && (pKeyCode == 257 || pKeyCode == 335);
        boolean hitE = getFocused() == null && client.options.inventoryKey.matchesKey(pKeyCode, pScanCode);
        if (hitE || hitEnter || hitEscape) {
            stopEditing();
            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (editingItem != null)
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        float chaseTarget = scroll.getChaseTarget();
        float max = 40 - (3 + AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight() * slices);
        max += schedule.size() * CARD_HEADER + 24;
        if (max > 0) {
            chaseTarget -= (float) (scrollY * 12);
            chaseTarget = MathHelper.clamp(chaseTarget, 0, max);
            scroll.chase((int) chaseTarget, 0.7f, Chaser.EXP);
        } else
            scroll.chase(0, 0.7f, Chaser.EXP);

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderForeground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);

        action(graphics, mouseX, mouseY, -1);

        if (editingItem == null)
            return;

        if (focusedSlot != null && focusedSlot.inventory == handler.proxyInventory && focusedSlot.getStack().isEmpty()) {
            graphics.drawTooltip(
                textRenderer, List.of(
                    CreateLang.translate("gui.stock_ticker.category_filter").color(ScrollInput.HEADER_RGB).component(),
                    CreateLang.translate("gui.stock_ticker.category_filter_tip").style(Formatting.GRAY).component(),
                    CreateLang.translate("gui.stock_ticker.category_filter_tip_1").style(Formatting.GRAY).component()
                ), mouseX, mouseY
            );
        }

        if (editorEditBox != null && editorEditBox.isHovered() && !editorEditBox.isFocused()) {
            graphics.drawTooltip(
                textRenderer,
                List.of(CreateLang.translate("gui.stock_ticker.category_name").color(ScrollInput.HEADER_RGB).component(), clickToEdit),
                mouseX,
                mouseY
            );
        }

    }

    @Override
    protected void drawBackground(DrawContext graphics, float pPartialTick, int pMouseX, int pMouseY) {
        pPartialTick = AnimationTickHolder.getPartialTicksUI(client.getRenderTickCounter());
        int y = this.y;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, x, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();
        for (int i = 0; i < slices; i++) {
            AllGuiTextures.STOCK_KEEPER_CATEGORY.render(graphics, x, y);
            y += AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight();
        }
        AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.render(graphics, x, y);
        AllGuiTextures.STOCK_KEEPER_CATEGORY_SAYS.render(graphics, x + backgroundWidth - 6, y + 7);

        OrderedText formattedcharsequence = handler.contentHolder.getCachedState().getBlock().getName().asOrderedText();

        int center = x + (AllGuiTextures.STOCK_KEEPER_CATEGORY.getWidth()) / 2;
        graphics.drawText(
            textRenderer,
            formattedcharsequence,
            (center - textRenderer.getWidth(formattedcharsequence) / 2),
            this.y + 4,
            0xFF3D3C48,
            false
        );

        if (editingItem == null) {
            renderCategories(graphics, pMouseX, pMouseY, pPartialTick);
            return;
        }

        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);

        y = this.y - 5;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, x, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();
        AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.render(graphics, x, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.getHeight();
        AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.render(graphics, x, y);

        renderPlayerInventory(graphics, x + 10, this.y + 88);

        formattedcharsequence = CreateLang.translate("gui.stock_ticker.category_editor").component().asOrderedText();
        graphics.drawText(
            textRenderer,
            formattedcharsequence,
            (center - textRenderer.getWidth(formattedcharsequence) / 2),
            this.y - 1,
            0x3D3C48,
            false
        );
    }

    @Override
    public void removed() {
        super.removed();
        client.player.networkHandler.sendPacket(new StockKeeperCategoryEditPacket(handler.contentHolder.getPos(), schedule));
    }

    @Override
    protected List<Text> getTooltipFromItem(ItemStack pStack) {
        List<Text> tooltip = super.getTooltipFromItem(pStack);
        if (focusedSlot == null || focusedSlot.inventory != handler.proxyInventory)
            return tooltip;
        if (!tooltip.isEmpty())
            tooltip.set(0, CreateLang.translate("gui.stock_ticker.category_filter").color(ScrollInput.HEADER_RGB).component());
        return tooltip;
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

    public TextRenderer getFont() {
        return textRenderer;
    }

}
