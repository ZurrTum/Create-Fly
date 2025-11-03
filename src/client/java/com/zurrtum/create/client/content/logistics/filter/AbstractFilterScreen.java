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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public abstract class AbstractFilterScreen<F extends AbstractFilterMenu> extends AbstractSimiContainerScreen<F> {

    protected AllGuiTextures background;
    private List<Rect2i> extraAreas = Collections.emptyList();

    private IconButton resetButton;
    private IconButton confirmButton;
    private ElementWidget renderedItem;

    protected AbstractFilterScreen(F menu, PlayerInventory inv, Text title, AllGuiTextures background) {
        super(menu, inv, title);
        this.background = background;
    }

    @Override
    protected void init() {
        setWindowSize(Math.max(background.getWidth(), PLAYER_INVENTORY.getWidth()), background.getHeight() + 4 + PLAYER_INVENTORY.getHeight());
        super.init();

        resetButton = new IconButton(x + background.getWidth() - 62, y + background.getHeight() - 24, AllIcons.I_TRASH);
        resetButton.withCallback(() -> {
            handler.clearContents();
            contentsCleared();
            client.player.networkHandler.sendPacket(AllPackets.CLEAR_CONTAINER);
        });
        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            client.player.closeHandledScreen();
        });

        addDrawableChild(resetButton);
        addDrawableChild(confirmButton);

        extraAreas = ImmutableList.of(new Rect2i(x + background.getWidth(), y + background.getHeight() - 40, 80, 48));

        renderedItem = new ElementWidget(
            x + background.getWidth() + 8,
            y + background.getHeight() - 52
        ).showingElement(GuiGameElement.of(handler.contentHolder).scale(4));
        addDrawableChild(renderedItem);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void drawBackground(DrawContext graphics, float partialTicks, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
        int invY = y + background.getHeight() + 4;
        renderPlayerInventory(graphics, invX, invY);

        background.render(graphics, x, y);
        graphics.drawText(textRenderer, title, x + (background.getWidth() - 8) / 2 - textRenderer.getWidth(title) / 2, y + 4, getTitleColor(), false);
    }

    protected int getTitleColor() {
        return 0xFF592424;
    }

    @Override
    protected void handledScreenTick() {
        if (!ItemStack.areEqual(client.player.getMainHandStack(), handler.contentHolder))
            client.player.closeHandledScreen();

        super.handledScreenTick();

        handleTooltips();
        handleIndicators();
    }

    protected void handleTooltips() {
        List<IconButton> tooltipButtons = getTooltipButtons();

        for (IconButton button : tooltipButtons) {
            if (!button.getToolTip().isEmpty()) {
                button.setToolTip(button.getToolTip().get(0));
                button.getToolTip().add(TooltipHelper.holdShift(Palette.YELLOW, AllKeys.hasShiftDown()));
            }
        }

        if (AllKeys.hasShiftDown()) {
            List<MutableText> tooltipDescriptions = getTooltipDescriptions();
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

    protected List<MutableText> getTooltipDescriptions() {
        return Collections.emptyList();
    }

    private void fillToolTip(IconButton button, Text tooltip) {
        if (!button.isSelected())
            return;
        List<Text> tip = button.getToolTip();
        tip.addAll(TooltipHelper.cutTextComponent(tooltip, Palette.ALL_GRAY));
    }

    protected void contentsCleared() {
    }

    protected void sendOptionUpdate(Option option) {
        client.player.networkHandler.sendPacket(new FilterScreenPacket(option));
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
