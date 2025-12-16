package com.zurrtum.create.client.content.redstone.link.controller;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.ControlsUtil;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.redstone.link.controller.LinkedControllerMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public class LinkedControllerScreen extends AbstractSimiContainerScreen<LinkedControllerMenu> {

    protected AllGuiTextures background;
    private List<Rect2i> extraAreas = Collections.emptyList();

    private IconButton resetButton;
    private IconButton confirmButton;
    private ElementWidget renderedItem;

    public LinkedControllerScreen(LinkedControllerMenu menu, Inventory inv, Component title) {
        super(
            menu,
            inv,
            title,
            AllGuiTextures.LINKED_CONTROLLER.getWidth(),
            AllGuiTextures.LINKED_CONTROLLER.getHeight() + 4 + PLAYER_INVENTORY.getHeight()
        );
        this.background = AllGuiTextures.LINKED_CONTROLLER;
    }

    public static LinkedControllerScreen create(
        Minecraft mc,
        MenuType<ItemStack> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        return type.create(LinkedControllerScreen::new, syncId, inventory, title, getStack(extraData));
    }

    @Override
    protected void init() {
        setWindowOffset(1, 0);
        super.init();

        resetButton = new IconButton(leftPos + imageWidth - 62, topPos + background.getHeight() - 24, AllIcons.I_TRASH);
        resetButton.withCallback(() -> {
            menu.clearContents();
            minecraft.player.connection.send(AllPackets.CLEAR_CONTAINER);
        });
        confirmButton = new IconButton(leftPos + imageWidth - 33, topPos + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            minecraft.player.closeContainer();
        });

        addRenderableWidget(resetButton);
        addRenderableWidget(confirmButton);

        extraAreas = ImmutableList.of(new Rect2i(leftPos + imageWidth + 4, topPos + background.getHeight() - 44, 64, 56));
        renderedItem = new ElementWidget(
            leftPos + imageWidth - 4,
            topPos + background.getHeight() - 56
        ).showingElement(GuiGameElement.of(menu.contentHolder).scale(5));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
        int invY = topPos + background.getHeight() + 4;
        renderPlayerInventory(graphics, invX, invY);

        background.render(graphics, leftPos, topPos);
        graphics.drawString(font, title, leftPos + 15, topPos + 4, 0xff592424, false);
    }

    @Override
    protected void containerTick() {
        if (!ItemStack.matches(menu.player.getMainHandItem(), menu.contentHolder))
            minecraft.player.closeContainer();

        super.containerTick();
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        if (!menu.getCarried().isEmpty() || hoveredSlot == null || hoveredSlot.container == menu.playerInventory) {
            super.renderTooltip(graphics, x, y);
            return;
        }

        List<Component> list = new LinkedList<>();
        if (hoveredSlot.hasItem())
            list = getTooltipFromContainerItem(hoveredSlot.getItem());

        graphics.setComponentTooltipForNextFrame(font, addToTooltip(list, hoveredSlot.getContainerSlot()), x, y);
    }

    private List<Component> addToTooltip(List<Component> list, int slot) {
        if (slot < 0 || slot >= 12)
            return list;
        list.add(CreateLang.translateDirect(
            "linked_controller.frequency_slot_" + ((slot % 2) + 1),
            ControlsUtil.getControls().get(slot / 2).getTranslatedKeyMessage().getString()
        ).withStyle(ChatFormatting.GOLD));
        return list;
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
