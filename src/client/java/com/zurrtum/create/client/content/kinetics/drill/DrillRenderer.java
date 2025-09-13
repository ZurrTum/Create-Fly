package com.zurrtum.create.client.content.kinetics.drill;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.drill.DrillBlock;
import com.zurrtum.create.content.kinetics.drill.DrillBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.util.math.Direction;

public class DrillRenderer extends KineticBlockEntityRenderer<DrillBlockEntity> {
    public DrillRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(DrillBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.DRILL_HEAD, state);
    }

    public static void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {
        BlockState state = context.state;
        SuperByteBuffer superBuffer = CachedBuffers.partial(AllPartialModels.DRILL_HEAD, state);
        Direction facing = state.get(DrillBlock.FACING);

        float speed = context.contraption.stalled || !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            facing.getOpposite()
        ) ? context.getAnimationSpeed() : 0;
        float time = AnimationTickHolder.getRenderTime() / 20;
        float angle = ((time * speed) % 360);

        superBuffer.transform(matrices.getModel()).center().rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(AngleHelper.verticalAngle(facing)).rotateZDegrees(angle).uncenter()
            .light(WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos)).useLevelLight(context.world, matrices.getWorld())
            .renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderLayer.getSolid()));
    }
}
