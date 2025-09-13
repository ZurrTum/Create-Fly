package com.zurrtum.create.client.content.equipment.toolbox;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.ErrorReporter;

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

    public ToolboxScreen(ToolboxMenu menu, PlayerInventory inv, Text title) {
        super(menu, inv, title);
        init();
    }

    public static ToolboxScreen create(
        MinecraftClient mc,
        MenuType<ToolboxBlockEntity> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        ToolboxBlockEntity entity = getBlockEntity(mc, extraData);
        if (entity == null) {
            return null;
        }
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getReporterContext(), Create.LOGGER)) {
            ReadView view = NbtReadView.create(logging, extraData.getRegistryManager(), extraData.readNbt());
            entity.readClient(view);
            return type.create(ToolboxScreen::new, syncId, inventory, title, entity);
        }
    }

    @Override
    protected void init() {
        setWindowSize(30 + BG.getWidth(), BG.getHeight() + PLAYER.getHeight() - 24);
        setWindowOffset(-11, 0);
        super.init();
        clearChildren();

        color = handler.contentHolder.getColor();

        confirmButton = new IconButton(x + 30 + BG.getWidth() - 33, y + BG.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            client.player.closeHandledScreen();
        });
        addDrawableChild(confirmButton);

        disposeButton = new IconButton(x + 30 + 81, y + 69, AllIcons.I_TOOLBOX);
        disposeButton.withCallback(() -> {
            client.player.networkHandler.sendPacket(new ToolboxDisposeAllPacket(handler.contentHolder.getPos()));
        });
        disposeButton.setToolTip(CreateLang.translateDirect("toolbox.depositBox"));
        addDrawableChild(disposeButton);

        extraAreas = ImmutableList.of(new Rect2i(x + 30 + BG.getWidth(), y + BG.getHeight() - 15 - 34 - 6, 72, 68));

        int x1 = x + backgroundWidth - 1;
        int y1 = y + BG.getHeight() - 13;
        renderedTopDrawer = new ElementWidget(x1, y1).showingElement(GuiGameElement.of(AllPartialModels.TOOLBOX_DRAWER).scale(3.125F)
            .transform(this::transformTopDrawer).atLocal(-0.2f, 0.4f));
        addDrawableChild(renderedTopDrawer);
        renderedItem = new ElementWidget(
            x + backgroundWidth + 5,
            y + BG.getHeight() - 54
        ).showingElement(GuiGameElement.of(handler.contentHolder.getCachedState().getBlock().getDefaultState()).scale(3.125F).rotate(-22, -202, 0)
            .padding(12));
        addDrawableChild(renderedItem);
        renderedLid = new ElementWidget(
            x + backgroundWidth + 10,
            y + BG.getHeight() - 58
        ).showingElement(GuiGameElement.of(AllPartialModels.TOOLBOX_LIDS.get(color)).scale(3.125F).transform(this::transformLid).padding(6));
        addDrawableChild(renderedLid);
        renderedBottomDrawer = new ElementWidget(x1, y + BG.getHeight() - 7).showingElement(GuiGameElement.of(AllPartialModels.TOOLBOX_DRAWER)
            .scale(3.125F).transform(this::transformBottomDrawer).atLocal(-0.2f, 0.4f));
        addDrawableChild(renderedBottomDrawer);
        renderedTopLeftDrawer = new ElementWidget(x1, y1).showingElement(GuiGameElement.of(AllPartialModels.TOOLBOX_DRAWER).scale(3.125F)
            .transform(this::transformTopDrawer).atLocal(-0.2f, 0.4f)).withScissor(0, 0, 15, 50);
        addDrawableChild(renderedTopLeftDrawer);
        renderedTopBottomDrawer = new ElementWidget(x1, y1).showingElement(GuiGameElement.of(AllPartialModels.TOOLBOX_DRAWER).scale(3.125F)
            .transform(this::transformTopDrawer).atLocal(-0.2f, 0.4f)).withScissor(0, 6, 50, 44);
        addDrawableChild(renderedTopBottomDrawer);
    }

    private void transformLid(MatrixStack ms, float partialTicks) {
        ms.translate(0.796F, 1.408F, 0);
        TransformStack.of(ms).rotateXDegrees(-22).rotateYDegrees(-202).translate(0, -6 / 16f, 12 / 16f)
            .rotateXDegrees(-105 * handler.contentHolder.lid.getValue(partialTicks)).translate(0, 6 / 16f, -12 / 16f);
        ms.scale(1, -1, 1);
    }

    private void transformTopDrawer(MatrixStack ms, float partialTicks) {
        ms.translate(1.02F, 0.384F, 0);
        TransformStack.of(ms).rotateXDegrees(-22).rotateYDegrees(-202).translate(0, 0, handler.contentHolder.drawers.getValue(partialTicks) * -.175f);
        ms.scale(1, -1, 1);
    }

    private void transformBottomDrawer(MatrixStack ms, float partialTicks) {
        ms.translate(1.02F, 0.38F, 0);
        TransformStack.of(ms).rotateXDegrees(-22).rotateYDegrees(-202)
            .translate(0, 0, handler.contentHolder.drawers.getValue(partialTicks) * -.175f * 2);
        ms.scale(1, -1, 1);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
        renderedLid.getRenderElement().clear();
        renderedTopDrawer.getRenderElement().clear();
        renderedBottomDrawer.getRenderElement().clear();
        renderedTopLeftDrawer.getRenderElement().clear();
        renderedTopBottomDrawer.getRenderElement().clear();
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        handler.renderPass = true;
        super.render(graphics, mouseX, mouseY, partialTicks);
        handler.renderPass = false;
    }

    @Override
    protected void drawBackground(DrawContext graphics, float partialTicks, int mouseX, int mouseY) {
        partialTicks = AnimationTickHolder.getPartialTicksUI(client.getRenderTickCounter());
        int x = this.x + backgroundWidth - BG.getWidth();

        BG.render(graphics, x, y);
        graphics.drawText(textRenderer, title, x + 15, y + 4, 0xFF592424, false);

        int invX = this.x;
        int invY = y + backgroundHeight - PLAYER.getHeight();
        renderPlayerInventory(graphics, invX, invY);

        ((GuiPartialRenderBuilder) renderedLid.getRenderElement()).tick(handler.contentHolder.lid.settled() ? 1 : partialTicks);
        float drawerTicks = handler.contentHolder.drawers.settled() ? 1 : partialTicks;
        ((GuiPartialRenderBuilder) renderedTopDrawer.getRenderElement()).tick(drawerTicks);
        ((GuiPartialRenderBuilder) renderedBottomDrawer.getRenderElement()).tick(drawerTicks);
        ((GuiPartialRenderBuilder) renderedTopLeftDrawer.getRenderElement()).tick(drawerTicks);
        ((GuiPartialRenderBuilder) renderedTopBottomDrawer.getRenderElement()).tick(drawerTicks);

        hoveredToolboxSlot = null;
        for (int compartment = 0; compartment < 8; compartment++) {
            int baseIndex = compartment * ToolboxInventory.STACKS_PER_COMPARTMENT;
            Slot slot = handler.slots.get(baseIndex);
            ItemStack itemstack = slot.getStack();
            int i = slot.x + this.x;
            int j = slot.y + y;

            if (itemstack.isEmpty())
                itemstack = handler.getFilter(compartment);

            if (isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                hoveredToolboxSlot = slot;
                int slotColor = 0x80FFFFFF;
                graphics.fillGradient(i, j, i + 16, j + 16, slotColor, slotColor);
            }

            if (!itemstack.isEmpty()) {
                int count = handler.totalCountInCompartment(compartment);
                String s = String.valueOf(count);
                graphics.drawItem(client.player, itemstack, i, j, 0);
                graphics.drawStackOverlay(textRenderer, itemstack, i, j, s);
            }
        }
    }

    @Override
    protected void renderForeground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        if (hoveredToolboxSlot != null)
            focusedSlot = hoveredToolboxSlot;
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
