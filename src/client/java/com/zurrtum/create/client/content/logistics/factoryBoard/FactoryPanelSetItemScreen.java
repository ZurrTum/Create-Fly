package com.zurrtum.create.client.content.logistics.factoryBoard;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelSetItemMenu;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Collections;
import java.util.List;

public class FactoryPanelSetItemScreen extends AbstractSimiContainerScreen<FactoryPanelSetItemMenu> {

    private IconButton confirmButton;
    private ElementWidget renderedItem;
    private List<Rect2i> extraAreas = Collections.emptyList();

    public FactoryPanelSetItemScreen(FactoryPanelSetItemMenu container, Inventory inv, Component title) {
        super(
            container,
            inv,
            title,
            AllGuiTextures.FACTORY_GAUGE_SET_ITEM.getWidth(),
            AllGuiTextures.FACTORY_GAUGE_SET_ITEM.getHeight() + AllGuiTextures.PLAYER_INVENTORY.getHeight()
        );
    }

    public static FactoryPanelSetItemScreen create(
        Minecraft mc,
        MenuType<ServerFactoryPanelBehaviour> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        FactoryPanelPosition pos = FactoryPanelPosition.PACKET_CODEC.decode(extraData);
        ServerFactoryPanelBehaviour behaviour = ServerFactoryPanelBehaviour.at(mc.level, pos);
        return type.create(FactoryPanelSetItemScreen::new, syncId, inventory, title, behaviour);
    }

    @Override
    protected void init() {
        int bgHeight = AllGuiTextures.FACTORY_GAUGE_SET_ITEM.getHeight();
        super.init();
        clearWidgets();

        confirmButton = new IconButton(leftPos + imageWidth - 40, topPos + bgHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> minecraft.player.closeContainer());
        addRenderableWidget(confirmButton);

        extraAreas = List.of(new Rect2i(leftPos + imageWidth, topPos + bgHeight - 30, 40, 20));

        renderedItem = new ElementWidget(leftPos + 180, topPos + 48).showingElement(GuiGameElement.of(AllItems.FACTORY_GAUGE.getDefaultInstance())
            .scale(3));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        AllGuiTextures.FACTORY_GAUGE_SET_ITEM.render(pGuiGraphics, leftPos - 5, topPos);
        renderPlayerInventory(pGuiGraphics, leftPos + 5, topPos + 94);

        Component title = CreateLang.translate("gui.factory_panel.place_item_to_monitor").component();
        pGuiGraphics.drawString(font, title, leftPos + imageWidth / 2 - font.width(title) / 2 - 5, topPos + 4, 0xFF3D3C48, false);
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
