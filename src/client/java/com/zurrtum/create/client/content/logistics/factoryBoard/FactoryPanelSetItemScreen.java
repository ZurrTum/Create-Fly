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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

public class FactoryPanelSetItemScreen extends AbstractSimiContainerScreen<FactoryPanelSetItemMenu> {

    private IconButton confirmButton;
    private ElementWidget renderedItem;
    private List<Rect2i> extraAreas = Collections.emptyList();

    public FactoryPanelSetItemScreen(FactoryPanelSetItemMenu container, PlayerInventory inv, Text title) {
        super(container, inv, title);
    }

    public static FactoryPanelSetItemScreen create(
        MinecraftClient mc,
        MenuType<ServerFactoryPanelBehaviour> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        FactoryPanelPosition pos = FactoryPanelPosition.PACKET_CODEC.decode(extraData);
        ServerFactoryPanelBehaviour behaviour = ServerFactoryPanelBehaviour.at(mc.world, pos);
        return type.create(FactoryPanelSetItemScreen::new, syncId, inventory, title, behaviour);
    }

    @Override
    protected void init() {
        int bgHeight = AllGuiTextures.FACTORY_GAUGE_SET_ITEM.getHeight();
        int bgWidth = AllGuiTextures.FACTORY_GAUGE_SET_ITEM.getWidth();
        setWindowSize(bgWidth, bgHeight + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        super.init();
        clearChildren();

        confirmButton = new IconButton(x + bgWidth - 40, y + bgHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> client.player.closeHandledScreen());
        addDrawableChild(confirmButton);

        extraAreas = List.of(new Rect2i(x + bgWidth, y + bgHeight - 30, 40, 20));

        renderedItem = new ElementWidget(x + 180, y + 48).showingElement(GuiGameElement.of(AllItems.FACTORY_GAUGE.getDefaultStack()).scale(3));
        addDrawableChild(renderedItem);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void drawBackground(DrawContext pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        AllGuiTextures.FACTORY_GAUGE_SET_ITEM.render(pGuiGraphics, x - 5, y);
        renderPlayerInventory(pGuiGraphics, x + 5, y + 94);

        Text title = CreateLang.translate("gui.factory_panel.place_item_to_monitor").component();
        pGuiGraphics.drawText(textRenderer, title, x + backgroundWidth / 2 - textRenderer.getWidth(title) / 2 - 5, y + 4, 0xFF3D3C48, false);
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
