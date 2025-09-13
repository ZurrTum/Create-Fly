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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

    public LinkedControllerScreen(LinkedControllerMenu menu, PlayerInventory inv, Text title) {
        super(menu, inv, title);
        this.background = AllGuiTextures.LINKED_CONTROLLER;
    }

    public static LinkedControllerScreen create(
        MinecraftClient mc,
        MenuType<ItemStack> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        return type.create(LinkedControllerScreen::new, syncId, inventory, title, getStack(extraData));
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight() + 4 + PLAYER_INVENTORY.getHeight());
        setWindowOffset(1, 0);
        super.init();

        resetButton = new IconButton(x + background.getWidth() - 62, y + background.getHeight() - 24, AllIcons.I_TRASH);
        resetButton.withCallback(() -> {
            handler.clearContents();
            client.player.networkHandler.sendPacket(AllPackets.CLEAR_CONTAINER);
        });
        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            client.player.closeHandledScreen();
        });

        addDrawableChild(resetButton);
        addDrawableChild(confirmButton);

        extraAreas = ImmutableList.of(new Rect2i(x + background.getWidth() + 4, y + background.getHeight() - 44, 64, 56));
        renderedItem = new ElementWidget(
            x + background.getWidth() - 4,
            y + background.getHeight() - 56
        ).showingElement(GuiGameElement.of(handler.contentHolder).scale(5));
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
        graphics.drawText(textRenderer, title, x + 15, y + 4, 0xff592424, false);
    }

    @Override
    protected void handledScreenTick() {
        if (!ItemStack.areEqual(handler.player.getMainHandStack(), handler.contentHolder))
            client.player.closeHandledScreen();

        super.handledScreenTick();
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext graphics, int x, int y) {
        if (!handler.getCursorStack().isEmpty() || focusedSlot == null || focusedSlot.inventory == handler.playerInventory) {
            super.drawMouseoverTooltip(graphics, x, y);
            return;
        }

        List<Text> list = new LinkedList<>();
        if (focusedSlot.hasStack())
            list = getTooltipFromItem(focusedSlot.getStack());

        graphics.drawTooltip(textRenderer, addToTooltip(list, focusedSlot.getIndex()), x, y);
    }

    private List<Text> addToTooltip(List<Text> list, int slot) {
        if (slot < 0 || slot >= 12)
            return list;
        list.add(CreateLang.translateDirect(
            "linked_controller.frequency_slot_" + ((slot % 2) + 1),
            ControlsUtil.getControls().get(slot / 2).getBoundKeyLocalizedText().getString()
        ).formatted(Formatting.GOLD));
        return list;
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
