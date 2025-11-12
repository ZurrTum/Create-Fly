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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

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

    public RedstoneRequesterScreen(RedstoneRequesterMenu container, Inventory inv, Component title) {
        super(container, inv, title);

        for (int i = 0; i < 9; i++)
            amounts.add(1);

        List<BigItemStack> stacks = menu.contentHolder.encodedRequest.stacks();
        for (int i = 0; i < stacks.size(); i++)
            amounts.set(i, Math.max(1, stacks.get(i).count));
    }

    public static RedstoneRequesterScreen create(
        Minecraft mc,
        MenuType<RedstoneRequesterBlockEntity> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        return type.create(RedstoneRequesterScreen::new, syncId, inventory, title, getBlockEntity(mc, extraData));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        addressBox.tick();
        for (int i = 0; i < amounts.size(); i++)
            if (menu.ghostInventory.getItem(i).isEmpty())
                amounts.set(i, 1);
    }

    @Override
    protected void init() {
        int bgHeight = AllGuiTextures.REDSTONE_REQUESTER.getHeight();
        int bgWidth = AllGuiTextures.REDSTONE_REQUESTER.getWidth();
        setWindowSize(bgWidth, bgHeight + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        super.init();
        clearWidgets();

        if (addressBox == null) {
            addressBox = new AddressEditBox(this, new NoShadowFontWrapper(font), leftPos + 55, topPos + 68, 110, 10, false);
            addressBox.setValue(menu.contentHolder.encodedTargetAdress);
            addressBox.setTextColor(0xFF555555);
        }
        addRenderableWidget(addressBox);

        confirmButton = new IconButton(leftPos + bgWidth - 30, topPos + bgHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> minecraft.player.closeContainer());
        addRenderableWidget(confirmButton);

        allowPartial = new IconButton(leftPos + 12, topPos + bgHeight - 25, AllIcons.I_PARTIAL_REQUESTS);
        allowPartial.withCallback(() -> {
            allowPartial.green = true;
            dontAllowPartial.green = false;
        });
        allowPartial.green = menu.contentHolder.allowPartialRequests;
        allowPartial.setToolTip(CreateLang.translate("gui.redstone_requester.allow_partial").component());
        addRenderableWidget(allowPartial);

        dontAllowPartial = new IconButton(leftPos + 12 + 18, topPos + bgHeight - 25, AllIcons.I_FULL_REQUESTS);
        dontAllowPartial.withCallback(() -> {
            allowPartial.green = false;
            dontAllowPartial.green = true;
        });
        dontAllowPartial.green = !menu.contentHolder.allowPartialRequests;
        dontAllowPartial.setToolTip(CreateLang.translate("gui.redstone_requester.dont_allow_partial").component());
        addRenderableWidget(dontAllowPartial);

        extraAreas = List.of(new Rect2i(leftPos + bgWidth, topPos + bgHeight - 50, 70, 60));
        renderedItem = new ElementWidget(
            leftPos + 245,
            topPos + 80
        ).showingElement(GuiGameElement.of(AllItems.REDSTONE_REQUESTER.getDefaultInstance()).scale(3));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        AllGuiTextures.REDSTONE_REQUESTER.render(pGuiGraphics, leftPos + 3, topPos);
        renderPlayerInventory(pGuiGraphics, leftPos - 3, topPos + 124);

        ItemStack stack = AllItems.REDSTONE_REQUESTER.getDefaultInstance();
        Component title = stack.getHoverName();
        pGuiGraphics.drawString(font, title, leftPos + 117 - font.width(title) / 2, topPos + 4, 0xFF3D3C48, false);
    }

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);

        for (int i = 0; i < amounts.size(); i++) {
            int inputX = leftPos + 27 + i * 20;
            int inputY = topPos + 28;
            ItemStack itemStack = menu.ghostInventory.getItem(i);
            if (itemStack.isEmpty())
                continue;
            graphics.renderItemDecorations(font, itemStack, inputX, inputY, "" + amounts.get(i));
        }

        if (addressBox.isHovered() && !addressBox.isFocused()) {
            if (addressBox.getValue().isBlank())
                graphics.setComponentTooltipForNextFrame(
                    font, List.of(
                        CreateLang.translate("gui.redstone_requester.requester_address").color(ScrollInput.HEADER_RGB).component(),
                        CreateLang.translate("gui.redstone_requester.requester_address_tip").style(ChatFormatting.GRAY).component(),
                        CreateLang.translate("gui.redstone_requester.requester_address_tip_1").style(ChatFormatting.GRAY).component(),
                        CreateLang.translate("gui.schedule.lmb_edit").style(ChatFormatting.DARK_GRAY).style(ChatFormatting.ITALIC).component()
                    ), mouseX, mouseY
                );
            else
                graphics.setComponentTooltipForNextFrame(
                    font, List.of(
                        CreateLang.translate("gui.redstone_requester.requester_address_given").color(ScrollInput.HEADER_RGB).component(),
                        CreateLang.text("'" + addressBox.getValue() + "'").style(ChatFormatting.GRAY).component()
                    ), mouseX, mouseY
                );
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (addressBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;

        for (int i = 0; i < amounts.size(); i++) {
            int inputX = leftPos + 27 + i * 20;
            int inputY = topPos + 28;
            if (mouseX >= inputX && mouseX < inputX + 16 && mouseY >= inputY && mouseY < inputY + 16) {
                ItemStack itemStack = menu.ghostInventory.getItem(i);
                if (itemStack.isEmpty())
                    return true;
                amounts.set(i, Mth.clamp((int) (amounts.get(i) + Math.signum(scrollY) * (AllKeys.hasShiftDown() ? 10 : 1)), 1, 256));
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack pStack) {
        List<Component> tooltip = super.getTooltipFromContainerItem(pStack);

        if (hoveredSlot == null || hoveredSlot.container != menu.ghostInventory) {
            return tooltip;
        }
        int slotIndex = hoveredSlot.getContainerSlot();
        if (slotIndex >= amounts.size())
            return tooltip;

        return List.of(
            CreateLang.translate("gui.factory_panel.send_item", CreateLang.itemName(pStack).add(CreateLang.text(" x" + amounts.get(slotIndex))))
                .color(ScrollInput.HEADER_RGB).component(),
            CreateLang.translate("gui.factory_panel.scroll_to_change_amount").style(ChatFormatting.DARK_GRAY).style(ChatFormatting.ITALIC)
                .component(),
            CreateLang.translate("gui.scrollInput.shiftScrollsFaster").style(ChatFormatting.DARK_GRAY).style(ChatFormatting.ITALIC).component()
        );
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

    @Override
    public void removed() {
        minecraft.player.connection.send(new RedstoneRequesterConfigurationPacket(
            menu.contentHolder.getBlockPos(),
            addressBox.getValue(),
            allowPartial.green,
            amounts
        ));
        super.removed();
    }

}
