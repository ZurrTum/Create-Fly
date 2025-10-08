package com.zurrtum.create.client.content.logistics.redstoneRequester;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.content.logistics.AddressEditBox;
import com.zurrtum.create.client.content.trains.station.NoShadowFontWrapper;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.packet.c2s.RedstoneRequesterConfigurationPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedstoneRequesterScreen extends AbstractSimiContainerScreen<RedstoneRequesterMenu> {

    private AddressEditBox addressBox;
    private IconButton confirmButton;
    private List<Rect2i> extraAreas = Collections.emptyList();
    private List<Integer> amounts = new ArrayList<>();

    private IconButton dontAllowPartial;
    private IconButton allowPartial;
    private ElementWidget renderedItem;

    public RedstoneRequesterScreen(RedstoneRequesterMenu container, PlayerInventory inv, Text title) {
        super(container, inv, title);

        for (int i = 0; i < 9; i++)
            amounts.add(1);

        List<BigItemStack> stacks = handler.contentHolder.encodedRequest.stacks();
        for (int i = 0; i < stacks.size(); i++)
            amounts.set(i, Math.max(1, stacks.get(i).count));
    }

    public static RedstoneRequesterScreen create(
        MinecraftClient mc,
        MenuType<RedstoneRequesterBlockEntity> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        return type.create(RedstoneRequesterScreen::new, syncId, inventory, title, getBlockEntity(mc, extraData));
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        addressBox.tick();
        for (int i = 0; i < amounts.size(); i++)
            if (handler.ghostInventory.getStack(i).isEmpty())
                amounts.set(i, 1);
    }

    @Override
    protected void init() {
        int bgHeight = AllGuiTextures.REDSTONE_REQUESTER.getHeight();
        int bgWidth = AllGuiTextures.REDSTONE_REQUESTER.getWidth();
        setWindowSize(bgWidth, bgHeight + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        super.init();
        clearChildren();

        if (addressBox == null) {
            addressBox = new AddressEditBox(this, new NoShadowFontWrapper(textRenderer), x + 55, y + 68, 110, 10, false);
            addressBox.setText(handler.contentHolder.encodedTargetAdress);
            addressBox.setEditableColor(0xFF555555);
        }
        addDrawableChild(addressBox);

        confirmButton = new IconButton(x + bgWidth - 30, y + bgHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> client.player.closeHandledScreen());
        addDrawableChild(confirmButton);

        allowPartial = new IconButton(x + 12, y + bgHeight - 25, AllIcons.I_PARTIAL_REQUESTS);
        allowPartial.withCallback(() -> {
            allowPartial.green = true;
            dontAllowPartial.green = false;
        });
        allowPartial.green = handler.contentHolder.allowPartialRequests;
        allowPartial.setToolTip(CreateLang.translate("gui.redstone_requester.allow_partial").component());
        addDrawableChild(allowPartial);

        dontAllowPartial = new IconButton(x + 12 + 18, y + bgHeight - 25, AllIcons.I_FULL_REQUESTS);
        dontAllowPartial.withCallback(() -> {
            allowPartial.green = false;
            dontAllowPartial.green = true;
        });
        dontAllowPartial.green = !handler.contentHolder.allowPartialRequests;
        dontAllowPartial.setToolTip(CreateLang.translate("gui.redstone_requester.dont_allow_partial").component());
        addDrawableChild(dontAllowPartial);

        extraAreas = List.of(new Rect2i(x + bgWidth, y + bgHeight - 50, 70, 60));
        renderedItem = new ElementWidget(x + 245, y + 80).showingElement(GuiGameElement.of(AllItems.REDSTONE_REQUESTER.getDefaultStack()).scale(3));
        addDrawableChild(renderedItem);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void drawBackground(DrawContext pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        AllGuiTextures.REDSTONE_REQUESTER.render(pGuiGraphics, x + 3, y);
        renderPlayerInventory(pGuiGraphics, x - 3, y + 124);

        ItemStack stack = AllItems.REDSTONE_REQUESTER.getDefaultStack();
        Text title = stack.getName();
        pGuiGraphics.drawText(textRenderer, title, x + 117 - textRenderer.getWidth(title) / 2, y + 4, 0xFF3D3C48, false);
    }

    @Override
    protected void renderForeground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);

        for (int i = 0; i < amounts.size(); i++) {
            int inputX = x + 27 + i * 20;
            int inputY = y + 28;
            ItemStack itemStack = handler.ghostInventory.getStack(i);
            if (itemStack.isEmpty())
                continue;
            graphics.drawStackOverlay(textRenderer, itemStack, inputX, inputY, "" + amounts.get(i));
        }

        if (addressBox.isHovered() && !addressBox.isFocused()) {
            if (addressBox.getText().isBlank())
                graphics.drawTooltip(
                    textRenderer, List.of(
                        CreateLang.translate("gui.redstone_requester.requester_address").color(ScrollInput.HEADER_RGB).component(),
                        CreateLang.translate("gui.redstone_requester.requester_address_tip").style(Formatting.GRAY).component(),
                        CreateLang.translate("gui.redstone_requester.requester_address_tip_1").style(Formatting.GRAY).component(),
                        CreateLang.translate("gui.schedule.lmb_edit").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
                    ), mouseX, mouseY
                );
            else
                graphics.drawTooltip(
                    textRenderer,
                    List.of(
                        CreateLang.translate("gui.redstone_requester.requester_address_given").color(ScrollInput.HEADER_RGB).component(),
                        CreateLang.text("'" + addressBox.getText() + "'").style(Formatting.GRAY).component()
                    ),
                    mouseX,
                    mouseY
                );
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (addressBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;

        for (int i = 0; i < amounts.size(); i++) {
            int inputX = x + 27 + i * 20;
            int inputY = y + 28;
            if (mouseX >= inputX && mouseX < inputX + 16 && mouseY >= inputY && mouseY < inputY + 16) {
                ItemStack itemStack = handler.ghostInventory.getStack(i);
                if (itemStack.isEmpty())
                    return true;
                amounts.set(i, MathHelper.clamp((int) (amounts.get(i) + Math.signum(scrollY) * (AllKeys.hasShiftDown() ? 10 : 1)), 1, 256));
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected List<Text> getTooltipFromItem(ItemStack pStack) {
        List<Text> tooltip = super.getTooltipFromItem(pStack);

        if (focusedSlot == null || focusedSlot.inventory != handler.ghostInventory) {
            return tooltip;
        }
        int slotIndex = focusedSlot.getIndex();
        if (slotIndex >= amounts.size())
            return tooltip;

        return List.of(
            CreateLang.translate("gui.factory_panel.send_item", CreateLang.itemName(pStack).add(CreateLang.text(" x" + amounts.get(slotIndex))))
                .color(ScrollInput.HEADER_RGB).component(),
            CreateLang.translate("gui.factory_panel.scroll_to_change_amount").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component(),
            CreateLang.translate("gui.scrollInput.shiftScrollsFaster").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
        );
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

    @Override
    public void removed() {
        client.player.networkHandler.sendPacket(new RedstoneRequesterConfigurationPacket(
            handler.contentHolder.getPos(),
            addressBox.getText(),
            allowPartial.green,
            amounts
        ));
        super.removed();
    }

}
