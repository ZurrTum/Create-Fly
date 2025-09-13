package com.zurrtum.create.client.content.contraptions.actors.trainControls;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class ControlsRenderer {
    public static void render(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer,
        float equipAnimation,
        float firstLever,
        float secondLever
    ) {
        BlockState state = context.state;
        Direction facing = state.get(ControlsBlock.FACING);

        SuperByteBuffer cover = CachedBuffers.partial(AllPartialModels.TRAIN_CONTROLS_COVER, state);
        float hAngle = 180 + AngleHelper.horizontalAngle(facing);
        MatrixStack ms = matrices.getModel();
        cover.transform(ms).center().rotateYDegrees(hAngle).uncenter().light(WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos))
            .useLevelLight(context.world, matrices.getWorld())
            .renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderLayer.getCutoutMipped()));

        double yOffset = MathHelper.lerp(equipAnimation * equipAnimation, -0.15f, 0.05f);

        for (boolean first : Iterate.trueAndFalse) {
            float vAngle = MathHelper.clamp(first ? firstLever * 70 - 25 : secondLever * 15, -45, 45);
            SuperByteBuffer lever = CachedBuffers.partial(AllPartialModels.TRAIN_CONTROLS_LEVER, state);
            ms.push();
            TransformStack.of(ms).center().rotateYDegrees(hAngle).translate(0, 4 / 16f, 4 / 16f).rotateXDegrees(vAngle - 45).translate(0, yOffset, 0)
                .rotateXDegrees(45).uncenter().translate(0, -6 / 16f, -3 / 16f).translate(first ? 0 : 6 / 16f, 0, 0);
            lever.transform(ms).light(WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos))
                .useLevelLight(context.world, matrices.getWorld()).renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderLayer.getSolid()));
            ms.pop();
        }
    }
}
