package com.zurrtum.create.client.foundation.gui.render;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class SandPaperRenderer extends PictureInPictureRenderer<SandPaperRenderState> {
    private final ItemStackRenderState renderState = new ItemStackRenderState();
    private final Supplier<ItemStack> stack = Suppliers.memoize(() -> {
        ItemStack stack = AllItems.SAND_PAPER.getDefaultInstance();
        stack.set(AllDataComponents.SAND_PAPER_JEI, Unit.INSTANCE);
        return stack;
    });

    @Override
    protected void renderToTexture(SandPaperRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Pose pose = matrices.last();
        pose.translate(0, -0.35f, 0);
        pose.scale(1, -1, -1);
        Minecraft mc = Minecraft.getInstance();
        Lighting lighting = mc.gameRenderer.lighting();
        lighting.setupFor(Lighting.Entry.ITEMS_FLAT);
        ItemStack renderStack = stack.get();
        renderStack.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(state.stack()));
        mc.getItemModelResolver().updateForTopItem(renderState, renderStack, ItemDisplayContext.GUI, null, null, 0);
        renderState.submit(matrices, queue, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
    }

    @Override
    protected String getTextureLabel() {
        return "Sand Paper";
    }

    @Override
    public Class<SandPaperRenderState> getRenderStateClass() {
        return SandPaperRenderState.class;
    }
}
