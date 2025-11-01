package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class BlazeBurnerElementRenderer extends SpecialGuiElementRenderer<BlazeBurnerRenderState> {
    public BlazeBurnerElementRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void render(BlazeBurnerRenderState state, MatrixStack matrices) {
        MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-22.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45));
        matrices.scale(1, -1, 1);
        float horizontalAngle = AngleHelper.rad(270);
        boolean canDrawFlame = state.heatLevel().isAtLeast(HeatLevel.FADING);
        PartialModel drawHat = AllPartialModels.LOGISTICS_HAT;

        VertexConsumer cutout = vertexConsumers.getBuffer(RenderLayer.getCutoutMipped());
        CachedBuffers.partial(AllPartialModels.BLAZE_CAGE, state.block()).rotateCentered(horizontalAngle + MathHelper.PI, Direction.UP)
            .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).renderInto(matrices.peek(), cutout);

        RenderDispatcher renderDispatcher = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher();
        OrderedRenderCommandQueueImpl queue = renderDispatcher.getQueue();
        BlazeBurnerRenderer.getBlazeBurnerRenderData(
            state.world(),
            state.block(),
            state.heatLevel(),
            state.animation(),
            horizontalAngle,
            canDrawFlame,
            state.drawGoggles(),
            drawHat,
            state.hash()
        ).render(matrices, queue);
        renderDispatcher.render();
    }

    @Override
    protected float getYOffset(int height, int windowScaleFactor) {
        return height / 1.6f;
    }

    @Override
    protected String getName() {
        return "Blaze Burner";
    }

    @Override
    public Class<BlazeBurnerRenderState> getElementClass() {
        return BlazeBurnerRenderState.class;
    }
}
