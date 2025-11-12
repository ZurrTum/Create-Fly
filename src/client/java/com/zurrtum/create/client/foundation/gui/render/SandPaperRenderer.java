package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SandPaperRenderer extends PictureInPictureRenderer<SandPaperRenderState> {
    private final ItemStackRenderState renderState = new ItemStackRenderState();
    private final ItemStack stack;

    public SandPaperRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
        stack = AllItems.SAND_PAPER.getDefaultInstance();
        stack.set(AllDataComponents.SAND_PAPER_JEI, Unit.INSTANCE);
    }

    @Override
    protected void renderToTexture(SandPaperRenderState state, PoseStack matrices) {
        matrices.translate(0, -0.35f, 0);
        matrices.scale(1, -1, -1);
        Minecraft mc = Minecraft.getInstance();
        Lighting lighting = mc.gameRenderer.getLighting();
        lighting.setupFor(Lighting.Entry.ITEMS_FLAT);
        stack.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(state.stack()));
        FeatureRenderDispatcher renderDispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
        mc.getItemModelResolver().updateForTopItem(renderState, stack, ItemDisplayContext.GUI, null, null, 0);
        renderState.submit(matrices, renderDispatcher.getSubmitNodeStorage(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        renderDispatcher.renderAllFeatures();
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
