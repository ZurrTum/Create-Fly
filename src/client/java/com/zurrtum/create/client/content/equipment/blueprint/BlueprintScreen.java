package com.zurrtum.create.client.content.equipment.blueprint;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity.BlueprintSection;
import com.zurrtum.create.content.equipment.blueprint.BlueprintMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.RotationAxis;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public class BlueprintScreen extends AbstractSimiContainerScreen<BlueprintMenu> {

    protected AllGuiTextures background;
    private List<Rect2i> extraAreas = Collections.emptyList();

    private IconButton resetButton;
    private IconButton confirmButton;
    private ElementWidget renderedItem;

    public BlueprintScreen(BlueprintMenu menu, PlayerInventory inv, Text title) {
        super(menu, inv, title);
        this.background = AllGuiTextures.BLUEPRINT;
    }

    public static BlueprintScreen create(
        MinecraftClient mc,
        MenuType<BlueprintSection> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        int entityID = extraData.readVarInt();
        int section = extraData.readVarInt();
        Entity entityByID = mc.world.getEntityById(entityID);
        if (!(entityByID instanceof BlueprintEntity blueprintEntity))
            return null;
        return type.create(BlueprintScreen::new, syncId, inventory, title, blueprintEntity.getSection(section));
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight() + 4 + PLAYER_INVENTORY.getHeight());
        setWindowOffset(1, 0);
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

        extraAreas = ImmutableList.of(new Rect2i(x + background.getWidth(), y + background.getHeight() - 36, 56, 44));

        renderedItem = new ElementWidget(x + background.getWidth() + 1, y + background.getHeight() - 34).showingElement(GuiGameElement.of(
            AllPartialModels.CRAFTING_BLUEPRINT_1x1).scale(2.5F).transform(this::transform).padding(13));
        addDrawableChild(renderedItem);
    }

    private void transform(MatrixStack ms, float p) {
        ms.translate(0.48F, 0.04F, 0);
        ms.scale(1, -1, 1);
        ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(22.5F));
        ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45F));
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45F));
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
        graphics.drawText(textRenderer, title, x + 15, y + 4, 0xFFFFFFFF, false);

        //TODO
        //        GuiGameElement.of(AllPartialModels.CRAFTING_BLUEPRINT_1x1)
        //            .<GuiGameElement.GuiRenderBuilder>at(x + background.getWidth() + 20, y + background.getHeight() - 32, 0).rotate(45, -45, 22.5f).scale(40)
        //            .render(graphics);
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
        if (slot < 0 || slot > 10)
            return list;

        if (slot < 9) {
            list.add(CreateLang.translateDirect("crafting_blueprint.crafting_slot").formatted(Formatting.GOLD));
            list.add(CreateLang.translateDirect("crafting_blueprint.filter_items_viable").formatted(Formatting.GRAY));
        } else if (slot == 9) {
            list.add(CreateLang.translateDirect("crafting_blueprint.display_slot").formatted(Formatting.GOLD));
        } else {
            list.add(CreateLang.translateDirect("crafting_blueprint.secondary_display_slot").formatted(Formatting.GOLD));
            list.add(CreateLang.translateDirect("crafting_blueprint.optional").formatted(Formatting.GRAY));
        }

        return list;
    }

    @Override
    protected void handledScreenTick() {
        if (!handler.contentHolder.isEntityAlive())
            client.player.closeHandledScreen();

        super.handledScreenTick();
    }

    protected void contentsCleared() {
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
