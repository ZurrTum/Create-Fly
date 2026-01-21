package com.zurrtum.create.client.content.logistics.filter;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.catnip.lang.FontHelper.Palette;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.content.logistics.filter.AbstractFilterMenu;
import com.zurrtum.create.infrastructure.packet.c2s.FilterScreenPacket;
import com.zurrtum.create.infrastructure.packet.c2s.FilterScreenPacket.Option;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public abstract class AbstractFilterScreen<F extends AbstractFilterMenu> extends AbstractSimiContainerScreen<F> {

    protected AllGuiTextures background;
    private List<Rect2i> extraAreas = Collections.emptyList();

    private IconButton resetButton;
    private IconButton confirmButton;
    private ElementWidget renderedItem;

    protected AbstractFilterScreen(F menu, Inventory inv, Component title, AllGuiTextures background) {
        super(
            menu,
            inv,
            title,
            Math.max(background.getWidth(), PLAYER_INVENTORY.getWidth()),
            background.getHeight() + 4 + PLAYER_INVENTORY.getHeight()
        );
        this.background = background;
    }

    @Override
    protected void init() {
        super.init();

        resetButton = new IconButton(leftPos + background.getWidth() - 62, topPos + background.getHeight() - 24, AllIcons.I_TRASH);
        resetButton.withCallback(() -> {
            menu.clearContents();
            contentsCleared();
            minecraft.player.connection.send(AllPackets.CLEAR_CONTAINER);
        });
        confirmButton = new IconButton(leftPos + background.getWidth() - 33, topPos + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            minecraft.player.closeContainer();
        });

        addRenderableWidget(resetButton);
        addRenderableWidget(confirmButton);

        extraAreas = ImmutableList.of(new Rect2i(leftPos + background.getWidth(), topPos + background.getHeight() - 40, 80, 48));

        renderedItem = new ElementWidget(leftPos + background.getWidth() + 8, topPos + background.getHeight() - 52).showingElement(GuiGameElement.of(
            menu.contentHolder).scale(4));
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
        graphics.drawString(font, title, leftPos + (background.getWidth() - 8) / 2 - font.width(title) / 2, topPos + 4, getTitleColor(), false);
    }

    protected int getTitleColor() {
        return 0xFF592424;
    }

    @Override
    protected void containerTick() {
        if (!ItemStack.matches(minecraft.player.getMainHandItem(), menu.contentHolder))
            minecraft.player.closeContainer();

        super.containerTick();

        handleTooltips();
        handleIndicators();
    }

    protected void handleTooltips() {
        List<IconButton> tooltipButtons = getTooltipButtons();

        for (IconButton button : tooltipButtons) {
            if (!button.getToolTip().isEmpty()) {
                button.setToolTip(button.getToolTip().getFirst());
                button.getToolTip().add(TooltipHelper.holdShift(Palette.YELLOW, AllKeys.hasShiftDown()));
            }
        }

        if (AllKeys.hasShiftDown()) {
            List<MutableComponent> tooltipDescriptions = getTooltipDescriptions();
            for (int i = 0; i < tooltipButtons.size(); i++)
                fillToolTip(tooltipButtons.get(i), tooltipDescriptions.get(i));
        }
    }

    public void handleIndicators() {
        for (IconButton button : getTooltipButtons())
            button.green = !isButtonEnabled(button);
    }

    protected abstract boolean isButtonEnabled(IconButton button);

    protected List<IconButton> getTooltipButtons() {
        return Collections.emptyList();
    }

    protected List<MutableComponent> getTooltipDescriptions() {
        return Collections.emptyList();
    }

    private void fillToolTip(IconButton button, Component tooltip) {
        if (!button.isHoveredOrFocused())
            return;
        List<Component> tip = button.getToolTip();
        tip.addAll(TooltipHelper.cutTextComponent(tooltip, Palette.ALL_GRAY));
    }

    protected void contentsCleared() {
    }

    protected void sendOptionUpdate(Option option) {
        minecraft.player.connection.send(new FilterScreenPacket(option));
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
