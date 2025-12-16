package com.zurrtum.create.client.content.equipment.toolbox;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.Create;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement.GuiPartialRenderBuilder;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import com.zurrtum.create.content.equipment.toolbox.ToolboxMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.packet.c2s.ToolboxDisposeAllPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.Collections;
import java.util.List;

public class ToolboxScreen extends AbstractSimiContainerScreen<ToolboxMenu> {

    protected static final AllGuiTextures BG = AllGuiTextures.TOOLBOX;
    protected static final AllGuiTextures PLAYER = AllGuiTextures.PLAYER_INVENTORY;

    protected Slot hoveredToolboxSlot;
    private IconButton confirmButton;
    private IconButton disposeButton;
    private ElementWidget renderedItem;
    private ElementWidget renderedLid;
    private ElementWidget renderedTopDrawer;
    private ElementWidget renderedBottomDrawer;
    private ElementWidget renderedTopLeftDrawer;
    private ElementWidget renderedTopBottomDrawer;
    private DyeColor color;

    private List<Rect2i> extraAreas = Collections.emptyList();

    public ToolboxScreen(ToolboxMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 30 + BG.getWidth(), BG.getHeight() + PLAYER.getHeight() - 24);
        init();
    }

    public static ToolboxScreen create(
        Minecraft mc,
        MenuType<ToolboxBlockEntity> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        ToolboxBlockEntity entity = getBlockEntity(mc, extraData);
        if (entity == null) {
            return null;
        }
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(entity.problemPath(), Create.LOGGER)) {
            ValueInput view = TagValueInput.create(logging, extraData.registryAccess(), extraData.readNbt());
            entity.readClient(view);
            return type.create(ToolboxScreen::new, syncId, inventory, title, entity);
        }
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 0);
        super.init();
        clearWidgets();

        color = menu.contentHolder.getColor();

        confirmButton = new IconButton(leftPos + 30 + BG.getWidth() - 33, topPos + BG.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            minecraft.player.closeContainer();
        });
        addRenderableWidget(confirmButton);

        disposeButton = new IconButton(leftPos + 30 + 81, topPos + 69, AllIcons.I_TOOLBOX);
        disposeButton.withCallback(() -> {
            minecraft.player.connection.send(new ToolboxDisposeAllPacket(menu.contentHolder.getBlockPos()));
        });
        disposeButton.setToolTip(CreateLang.translateDirect("toolbox.depositBox"));
        addRenderableWidget(disposeButton);

        extraAreas = ImmutableList.of(new Rect2i(leftPos + 30 + BG.getWidth(), topPos + BG.getHeight() - 15 - 34 - 6, 72, 68));

        int x1 = leftPos + imageWidth - 1;
        int y1 = topPos + BG.getHeight() - 13;
        renderedTopDrawer = new ElementWidget(x1, y1).showingElement(GuiGameElement.of(AllPartialModels.TOOLBOX_DRAWER).scale(3.125F)
            .transform(this::transformTopDrawer).atLocal(-0.2f, 0.4f));
        addRenderableWidget(renderedTopDrawer);
        renderedItem = new ElementWidget(
            leftPos + imageWidth + 5,
            topPos + BG.getHeight() - 54
        ).showingElement(GuiGameElement.of(menu.contentHolder.getBlockState().getBlock().defaultBlockState()).scale(3.125F).rotate(-22, -202, 0)
            .padding(12));
        addRenderableWidget(renderedItem);
        renderedLid = new ElementWidget(
            leftPos + imageWidth + 10,
            topPos + BG.getHeight() - 58
        ).showingElement(GuiGameElement.of(AllPartialModels.TOOLBOX_LIDS.get(color)).scale(3.125F).transform(this::transformLid).padding(6));
        addRenderableWidget(renderedLid);
        renderedBottomDrawer = new ElementWidget(x1, topPos + BG.getHeight() - 7).showingElement(GuiGameElement.of(AllPartialModels.TOOLBOX_DRAWER)
            .scale(3.125F).transform(this::transformBottomDrawer).atLocal(-0.2f, 0.4f));
        addRenderableWidget(renderedBottomDrawer);
        renderedTopLeftDrawer = new ElementWidget(x1, y1).showingElement(GuiGameElement.of(AllPartialModels.TOOLBOX_DRAWER).scale(3.125F)
            .transform(this::transformTopDrawer).atLocal(-0.2f, 0.4f)).withScissor(0, 0, 15, 50);
        addRenderableWidget(renderedTopLeftDrawer);
        renderedTopBottomDrawer = new ElementWidget(x1, y1).showingElement(GuiGameElement.of(AllPartialModels.TOOLBOX_DRAWER).scale(3.125F)
            .transform(this::transformTopDrawer).atLocal(-0.2f, 0.4f)).withScissor(0, 6, 50, 44);
        addRenderableWidget(renderedTopBottomDrawer);
    }

    private void transformLid(PoseStack ms, float partialTicks) {
        ms.translate(0.796F, 1.408F, 0);
        TransformStack.of(ms).rotateXDegrees(-22).rotateYDegrees(-202).translate(0, -6 / 16f, 12 / 16f)
            .rotateXDegrees(-105 * menu.contentHolder.lid.getValue(partialTicks)).translate(0, 6 / 16f, -12 / 16f);
        ms.scale(1, -1, 1);
    }

    private void transformTopDrawer(PoseStack ms, float partialTicks) {
        ms.translate(1.02F, 0.384F, 0);
        TransformStack.of(ms).rotateXDegrees(-22).rotateYDegrees(-202).translate(0, 0, menu.contentHolder.drawers.getValue(partialTicks) * -.175f);
        ms.scale(1, -1, 1);
    }

    private void transformBottomDrawer(PoseStack ms, float partialTicks) {
        ms.translate(1.02F, 0.38F, 0);
        TransformStack.of(ms).rotateXDegrees(-22).rotateYDegrees(-202)
            .translate(0, 0, menu.contentHolder.drawers.getValue(partialTicks) * -.175f * 2);
        ms.scale(1, -1, 1);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
        renderedLid.getRenderElement().clear();
        renderedTopDrawer.getRenderElement().clear();
        renderedBottomDrawer.getRenderElement().clear();
        renderedTopLeftDrawer.getRenderElement().clear();
        renderedTopBottomDrawer.getRenderElement().clear();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        menu.renderPass = true;
        super.render(graphics, mouseX, mouseY, partialTicks);
        menu.renderPass = false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        partialTicks = AnimationTickHolder.getPartialTicksUI(minecraft.getDeltaTracker());
        int x = this.leftPos + imageWidth - BG.getWidth();

        BG.render(graphics, x, topPos);
        graphics.drawString(font, title, x + 15, topPos + 4, 0xFF592424, false);

        int invX = this.leftPos;
        int invY = topPos + imageHeight - PLAYER.getHeight();
        renderPlayerInventory(graphics, invX, invY);

        ((GuiPartialRenderBuilder) renderedLid.getRenderElement()).tick(menu.contentHolder.lid.settled() ? 1 : partialTicks);
        float drawerTicks = menu.contentHolder.drawers.settled() ? 1 : partialTicks;
        ((GuiPartialRenderBuilder) renderedTopDrawer.getRenderElement()).tick(drawerTicks);
        ((GuiPartialRenderBuilder) renderedBottomDrawer.getRenderElement()).tick(drawerTicks);
        ((GuiPartialRenderBuilder) renderedTopLeftDrawer.getRenderElement()).tick(drawerTicks);
        ((GuiPartialRenderBuilder) renderedTopBottomDrawer.getRenderElement()).tick(drawerTicks);

        hoveredToolboxSlot = null;
        for (int compartment = 0; compartment < 8; compartment++) {
            int baseIndex = compartment * ToolboxInventory.STACKS_PER_COMPARTMENT;
            Slot slot = menu.slots.get(baseIndex);
            ItemStack itemstack = slot.getItem();
            int i = slot.x + this.leftPos;
            int j = slot.y + topPos;

            if (itemstack.isEmpty())
                itemstack = menu.getFilter(compartment);

            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                hoveredToolboxSlot = slot;
                int slotColor = 0x80FFFFFF;
                graphics.fillGradient(i, j, i + 16, j + 16, slotColor, slotColor);
            }

            if (!itemstack.isEmpty()) {
                int count = menu.totalCountInCompartment(compartment);
                String s = String.valueOf(count);
                graphics.renderItem(minecraft.player, itemstack, i, j, 0);
                graphics.renderItemDecorations(font, itemstack, i, j, s);
            }
        }
    }

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (hoveredToolboxSlot != null)
            hoveredSlot = hoveredToolboxSlot;
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
