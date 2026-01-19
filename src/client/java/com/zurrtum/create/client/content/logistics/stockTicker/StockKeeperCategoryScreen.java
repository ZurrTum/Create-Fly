package com.zurrtum.create.client.content.logistics.stockTicker;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.client.AllKeys;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
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
    private EditBox editorEditBox;
    private ElementWidget renderedItem;

    final int slices = 4;

    public StockKeeperCategoryScreen(StockKeeperCategoryMenu menu, Inventory inv, Component title) {
        super(
            menu,
            inv,
            title,
            AllGuiTextures.STOCK_KEEPER_CATEGORY.getWidth(),
            AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight() * 4 + AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight() + AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.getHeight()
        );
        schedule = new ArrayList<>(menu.contentHolder.categories);
        menu.slotsActive = false;
    }

    public static StockKeeperCategoryScreen create(
        Minecraft mc,
        MenuType<StockTickerBlockEntity> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        return type.create(StockKeeperCategoryScreen::new, syncId, inventory, title, getBlockEntity(mc, extraData));
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();

        confirmButton = new IconButton(leftPos + imageWidth - 25, topPos + imageHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> minecraft.player.closeContainer());
        addRenderableWidget(confirmButton);

        stopEditing();

        extraAreas = ImmutableList.of(new Rect2i(leftPos + imageWidth, topPos + imageHeight - 40, 48, 40));

        renderedItem = new ElementWidget(
            leftPos + imageWidth + 12,
            topPos + imageHeight - 39
        ).showingElement(GuiGameElement.of(AllItems.STOCK_TICKER.getDefaultInstance()).scale(3));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
    }

    protected void startEditing(int index) {
        confirmButton.visible = false;

        editorConfirm = new IconButton(leftPos + 36 + 131, topPos + 59, AllIcons.I_CONFIRM);
        menu.slotsActive = true;

        editorEditBox = new EditBox(font, leftPos + 47, topPos + 28, 124, 10, Component.empty());
        editorEditBox.setTextColor(0xffeeeeee);
        editorEditBox.setBordered(false);
        editorEditBox.setFocused(false);
        editorEditBox.mouseClicked(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)), false);
        editorEditBox.setMaxLength(28);
        editorEditBox.setValue(index == -1 || schedule.get(index).isEmpty() ? CreateLang.translate("gui.stock_ticker.new_category")
            .string() : schedule.get(index).getHoverName().getString());

        editingIndex = index;
        editingItem = index == -1 ? ItemStack.EMPTY : schedule.get(index);
        menu.proxyInventory.setItem(0, editingItem);
        minecraft.player.connection.send(new GhostItemSubmitPacket(editingItem, 0));

        addRenderableWidget(editorConfirm);
        addRenderableWidget(editorEditBox);
        //        imageHeight = 88 + AllGuiTextures.PLAYER_INVENTORY.getHeight();
    }

    protected void stopEditing() {
        confirmButton.visible = true;
        if (editingItem == null)
            return;

        playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1);
        removeWidget(editorConfirm);
        removeWidget(editorEditBox);

        ItemStack stackInSlot = menu.proxyInventory.getItem(0).copy();
        boolean empty = stackInSlot.isEmpty();

        if (empty && editingIndex != -1)
            schedule.remove(editingIndex);

        if (!empty) {
            String value = editorEditBox.getValue();
            boolean blank = value.isBlank() || value.equals(CreateLang.translate("gui.stock_ticker.new_category").string()) || value.equals(
                stackInSlot.getHoverName().getString());
            stackInSlot.set(DataComponents.CUSTOM_NAME, blank ? null : Component.literal(value));
            if (editingIndex == -1)
                schedule.add(stackInSlot);
            else
                schedule.set(editingIndex, stackInSlot);
        }

        minecraft.player.connection.send(new GhostItemSubmitPacket(ItemStack.EMPTY, 0));

        editingItem = null;
        editorConfirm = null;
        editorEditBox = null;
        menu.slotsActive = false;
        renderedItem.getRenderElement().clear();
        init();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        scroll.tickChaser();
        if (editorEditBox == null)
            return;
        if (!editorEditBox.getValue().equals(CreateLang.translate("gui.stock_ticker.new_category").string()))
            return;
        if (menu.proxyInventory.getItem(0).has(DataComponents.CUSTOM_NAME))
            editorEditBox.setValue(menu.proxyInventory.getItem(0).getHoverName().getString());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        partialTicks = AnimationTickHolder.getPartialTicksUI(minecraft.getDeltaTracker());

        if (menu.slotsActive)
            super.render(graphics, mouseX, mouseY, partialTicks);
        else {
            for (Renderable widget : renderables)
                widget.render(graphics, mouseX, mouseY, partialTicks);
            renderForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    protected void renderCategories(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Matrix3x2fStack matrixStack = graphics.pose();
        int yOffset = 25;
        List<ItemStack> entries = schedule;
        float scrollOffset = -scroll.getValue(partialTicks);

        graphics.enableScissor(leftPos + 3, topPos + 16, leftPos + 187, topPos + 19 + (AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight() * slices));
        for (int i = 0; i <= entries.size(); i++) {
            matrixStack.pushMatrix();
            matrixStack.translate(0, scrollOffset);

            if (i == entries.size()) {
                AllGuiTextures.STOCK_KEEPER_CATEGORY_NEW.render(graphics, leftPos + 7, topPos + yOffset);
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

    public int renderScheduleEntry(GuiGraphics graphics, Matrix3x2fStack matrixStack, int i, ItemStack entry, int yOffset) {
        int cardWidth = CARD_WIDTH;
        int cardHeader = CARD_HEADER;

        matrixStack.pushMatrix();
        matrixStack.translate(leftPos + 7, topPos + yOffset);

        AllGuiTextures.STOCK_KEEPER_CATEGORY_ENTRY.render(graphics, 0, 0);

        if (i > 0)
            AllGuiTextures.STOCK_KEEPER_CATEGORY_UP.render(graphics, cardWidth + 12, cardHeader - 18);
        if (i < schedule.size() - 1)
            AllGuiTextures.STOCK_KEEPER_CATEGORY_DOWN.render(graphics, cardWidth + 12, cardHeader - 9);

        graphics.renderItem(entry, 14, 1);
        Component name = entry.getHoverName();
        graphics.drawString(
            font,
            entry.isEmpty() ? CreateLang.translate("gui.stock_ticker.empty_category_name_placeholder").string() : name.getString(20)
                .stripTrailing() + (name.getString().length() > 20 ? "..." : ""),
            35,
            5,
            0xFF656565,
            false
        );

        matrixStack.popMatrix();
        return cardHeader;
    }

    private final Component clickToEdit = CreateLang.translateDirect("gui.schedule.lmb_edit")
        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);

    public boolean action(@Nullable InputWithModifiers input, @Nullable GuiGraphics graphics, double mouseX, double mouseY, int click) {
        // Prevent actions outside the window for them
        if (mouseX < this.leftPos || mouseX >= this.leftPos + imageWidth || mouseY < this.topPos + 15 || mouseY >= this.topPos + 99)
            return false;

        if (editingItem != null)
            return false;

        int mx = (int) mouseX;
        int my = (int) mouseY;
        int x = mx - this.leftPos - 20;
        int y = my - this.topPos - 24;
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
                List<Component> components = new ArrayList<>();
                components.add(entry.isEmpty() ? CreateLang.translate("gui.stock_ticker.empty_category_name_placeholder")
                    .component() : entry.getHoverName());
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
                        minecraft.player.connection.send(new StockKeeperCategoryRefundPacket(menu.contentHolder.getBlockPos(), entry));
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
                            CreateLang.translate("gui.stock_ticker.shift_moves_top").style(ChatFormatting.DARK_GRAY).style(ChatFormatting.ITALIC)
                                .component()
                        ), mx, my
                    );
                    if (click == 0) {
                        entries.remove(entry);
                        entries.add((input != null ? input.hasShiftDown() : AllKeys.hasShiftDown()) ? 0 : i - 1, entry);
                        renderedItem.getRenderElement().clear();
                        init();
                    }
                    return true;
                }
                if (y > 10 && y <= 22 && i < entries.size() - 1) {
                    renderActionTooltip(
                        graphics, ImmutableList.of(
                            CreateLang.translateDirect("gui.schedule.move_down"),
                            CreateLang.translate("gui.stock_ticker.shift_moves_bottom").style(ChatFormatting.DARK_GRAY).style(ChatFormatting.ITALIC)
                                .component()
                        ), mx, my
                    );
                    if (click == 0) {
                        entries.remove(entry);
                        entries.add((input != null ? input.hasShiftDown() : AllKeys.hasShiftDown()) ? entries.size() : i + 1, entry);
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

    private void renderActionTooltip(@Nullable GuiGraphics graphics, List<Component> tooltip, int mx, int my) {
        if (graphics != null)
            graphics.setTooltipForNextFrame(font, tooltip, Optional.empty(), mx, my);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double pMouseX = click.x();
        double pMouseY = click.y();
        if (editorConfirm != null && editorConfirm.isMouseOver(pMouseX, pMouseY)) {
            stopEditing();
            return true;
        }
        if (action(click, null, pMouseX, pMouseY, click.button())) {
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
            return true;
        }

        boolean wasNotFocused = editorEditBox != null && !editorEditBox.isFocused();
        boolean mouseClicked = super.mouseClicked(click, doubled);

        if (editorEditBox != null && editorEditBox.isMouseOver(pMouseX, pMouseY) && wasNotFocused) {
            editorEditBox.moveCursorToEnd(false);
            editorEditBox.setHighlightPos(0);
        }

        return mouseClicked;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (editingItem == null)
            return super.keyPressed(input);

        int pKeyCode = input.key();
        boolean hitEscape = pKeyCode == GLFW.GLFW_KEY_ESCAPE;
        boolean hitEnter = getFocused() instanceof EditBox && (pKeyCode == 257 || pKeyCode == 335);
        boolean hitE = getFocused() == null && minecraft.options.keyInventory.matches(input);
        if (hitE || hitEnter || hitEscape) {
            stopEditing();
            return true;
        }

        return super.keyPressed(input);
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
            chaseTarget = Mth.clamp(chaseTarget, 0, max);
            scroll.chase((int) chaseTarget, 0.7f, Chaser.EXP);
        } else
            scroll.chase(0, 0.7f, Chaser.EXP);

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);

        action(null, graphics, mouseX, mouseY, -1);

        if (editingItem == null)
            return;

        if (hoveredSlot != null && hoveredSlot.container == menu.proxyInventory && hoveredSlot.getItem().isEmpty()) {
            graphics.setComponentTooltipForNextFrame(
                font, List.of(
                    CreateLang.translate("gui.stock_ticker.category_filter").color(ScrollInput.HEADER_RGB).component(),
                    CreateLang.translate("gui.stock_ticker.category_filter_tip").style(ChatFormatting.GRAY).component(),
                    CreateLang.translate("gui.stock_ticker.category_filter_tip_1").style(ChatFormatting.GRAY).component()
                ), mouseX, mouseY
            );
        }

        if (editorEditBox != null && editorEditBox.isHovered() && !editorEditBox.isFocused()) {
            graphics.setComponentTooltipForNextFrame(
                font,
                List.of(CreateLang.translate("gui.stock_ticker.category_name").color(ScrollInput.HEADER_RGB).component(), clickToEdit),
                mouseX,
                mouseY
            );
        }

    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        pPartialTick = AnimationTickHolder.getPartialTicksUI(minecraft.getDeltaTracker());
        int y = this.topPos;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, leftPos, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();
        for (int i = 0; i < slices; i++) {
            AllGuiTextures.STOCK_KEEPER_CATEGORY.render(graphics, leftPos, y);
            y += AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight();
        }
        AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.render(graphics, leftPos, y);
        AllGuiTextures.STOCK_KEEPER_CATEGORY_SAYS.render(graphics, leftPos + imageWidth - 6, y + 7);

        FormattedCharSequence formattedcharsequence = menu.contentHolder.getBlockState().getBlock().getName().getVisualOrderText();

        int center = leftPos + (AllGuiTextures.STOCK_KEEPER_CATEGORY.getWidth()) / 2;
        graphics.drawString(font, formattedcharsequence, (center - font.width(formattedcharsequence) / 2), this.topPos + 4, 0xFF3D3C48, false);
        renderCategories(graphics, pMouseX, pMouseY, pPartialTick);

        if (editingItem == null) {
            renderCategories(graphics, pMouseX, pMouseY, pPartialTick);
            return;
        }

        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);

        y = this.topPos - 5;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, leftPos, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();
        AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.render(graphics, leftPos, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.getHeight();
        AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.render(graphics, leftPos, y);

        renderPlayerInventory(graphics, leftPos + 10, this.topPos + 88);

        formattedcharsequence = CreateLang.translate("gui.stock_ticker.category_editor").component().getVisualOrderText();
        graphics.drawString(font, formattedcharsequence, (center - font.width(formattedcharsequence) / 2), this.topPos - 1, 0x3D3C48, false);
    }

    @Override
    public void removed() {
        super.removed();
        minecraft.player.connection.send(new StockKeeperCategoryEditPacket(menu.contentHolder.getBlockPos(), schedule));
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack pStack) {
        List<Component> tooltip = super.getTooltipFromContainerItem(pStack);
        if (hoveredSlot == null || hoveredSlot.container != menu.proxyInventory)
            return tooltip;
        if (!tooltip.isEmpty())
            tooltip.set(0, CreateLang.translate("gui.stock_ticker.category_filter").color(ScrollInput.HEADER_RGB).component());
        return tooltip;
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

    public Font getFont() {
        return font;
    }

}
