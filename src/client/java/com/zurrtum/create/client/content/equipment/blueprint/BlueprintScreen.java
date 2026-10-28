package com.zurrtum.create.client.content.equipment.blueprint;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity.BlueprintSection;
import com.zurrtum.create.content.equipment.blueprint.BlueprintMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public class BlueprintScreen extends AbstractSimiContainerScreen<BlueprintMenu> {

    protected AllGuiTextures background;
    private List<Rect2i> extraAreas = Collections.emptyList();

    private @Nullable IconButton resetButton;
    private @Nullable IconButton confirmButton;
    private @Nullable ElementWidget renderedItem;

    public BlueprintScreen(BlueprintMenu menu, Inventory inv, Component title) {
        super(
            menu,
            inv,
            title,
            AllGuiTextures.BLUEPRINT.getWidth(),
            AllGuiTextures.BLUEPRINT.getHeight() + 4 + PLAYER_INVENTORY.getHeight()
        );
        background = AllGuiTextures.BLUEPRINT;
    }

    @Nullable
    public static BlueprintScreen create(
        Minecraft mc,
        MenuType<BlueprintSection> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        int entityID = extraData.readVarInt();
        int section = extraData.readVarInt();
        Entity entityByID = mc.level.getEntity(entityID);
        if (!(entityByID instanceof BlueprintEntity blueprintEntity)) {
            return null;
        }
        return type.create(BlueprintScreen::new, syncId, inventory, title, blueprintEntity.getSection(section));
    }

    @Override
    protected void init() {
        setWindowOffset(1, 0);
        super.init();

        resetButton = new IconButton(leftPos + imageWidth - 62, topPos + background.getHeight() - 24, AllIcons.I_TRASH);
        resetButton.withCallback(() -> {
            menu.clearContents();
            contentsCleared();
            minecraft.player.connection.send(AllPackets.CLEAR_CONTAINER);
        });
        confirmButton = new IconButton(
            leftPos + imageWidth - 33,
            topPos + background.getHeight() - 24,
            AllIcons.I_CONFIRM
        );
        confirmButton.withCallback(() -> {
            minecraft.player.closeContainer();
        });

        addRenderableWidget(resetButton);
        addRenderableWidget(confirmButton);

        extraAreas = ImmutableList.of(new Rect2i(leftPos + imageWidth, topPos + background.getHeight() - 36, 56, 44));

        renderedItem = new ElementWidget(leftPos + imageWidth + 1, topPos + background.getHeight() - 34).showingElement(
            GuiGameElement.of(AllPartialModels.CRAFTING_BLUEPRINT_1x1).scale(2.5F).transform(this::transform)
                .padding(13));
        addRenderableWidget(renderedItem);
    }

    private void transform(PoseStack ms, float p) {
        Pose pose = ms.last();
        pose.translate(0.48F, 0.04F, 0);
        pose.scale(1, -1, 1);
        pose.rotate(Axis.ZP, Radians.DEG_22P5);
        pose.rotate(Axis.XP, Radians.DEG_45);
        pose.rotate(Axis.YP, Radians.DEG_315);
    }

    @Override
    public void removed() {
        super.removed();
        renderedItem.getRenderElement().clear();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
        int invY = topPos + background.getHeight() + 4;
        renderPlayerInventory(graphics, invX, invY);

        background.render(graphics, leftPos, topPos);
        graphics.text(font, title, leftPos + 15, topPos + 4, 0xFFFFFFFF, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
        if (!menu.getCarried().isEmpty() || hoveredSlot == null || hoveredSlot.container == menu.playerInventory) {
            super.extractTooltip(graphics, x, y);
            return;
        }

        List<Component> list = new LinkedList<>();
        if (hoveredSlot.hasItem()) {
            list = getTooltipFromContainerItem(hoveredSlot.getItem());
        }

        graphics.setComponentTooltipForNextFrame(font, addToTooltip(list, hoveredSlot.getContainerSlot()), x, y);
    }

    private List<Component> addToTooltip(List<Component> list, int slot) {
        if (slot < 0 || slot > 10) {
            return list;
        }

        if (slot < 9) {
            list.add(CreateLang.translateDirect("crafting_blueprint.crafting_slot").withStyle(ChatFormatting.GOLD));
            list.add(CreateLang.translateDirect("crafting_blueprint.filter_items_viable")
                .withStyle(ChatFormatting.GRAY));
        } else if (slot == 9) {
            list.add(CreateLang.translateDirect("crafting_blueprint.display_slot").withStyle(ChatFormatting.GOLD));
        } else {
            list.add(CreateLang.translateDirect("crafting_blueprint.secondary_display_slot")
                .withStyle(ChatFormatting.GOLD));
            list.add(CreateLang.translateDirect("crafting_blueprint.optional").withStyle(ChatFormatting.GRAY));
        }

        return list;
    }

    @Override
    protected void containerTick() {
        if (!menu.contentHolder.isEntityAlive()) {
            minecraft.player.closeContainer();
        }

        super.containerTick();
    }

    protected void contentsCleared() {
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
