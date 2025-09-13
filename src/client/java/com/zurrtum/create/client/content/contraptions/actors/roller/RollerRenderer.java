package com.zurrtum.create.client.content.contraptions.actors.roller;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.actors.harvester.HarvesterRenderer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlock;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlockEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RollerRenderer extends SmartBlockEntityRenderer<RollerBlockEntity> {

    public RollerRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(RollerBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        BlockState blockState = be.getCachedState();
        VertexConsumer vc = buffer.getBuffer(RenderLayer.getCutoutMipped());

        ms.push();
        ms.translate(0, -0.25, 0);
        SuperByteBuffer superBuffer = CachedBuffers.partial(AllPartialModels.ROLLER_WHEEL, blockState);
        Direction facing = blockState.get(RollerBlock.FACING);
        superBuffer.translate(Vec3d.of(facing.getVector()).multiply(17 / 16f));
        HarvesterRenderer.transform(be.getWorld(), facing, superBuffer, be.getAnimatedSpeed(), Vec3d.ZERO);
        superBuffer.translate(0, -.5, .5).rotateYDegrees(90).light(light).renderInto(ms, vc);
        ms.pop();

        CachedBuffers.partial(AllPartialModels.ROLLER_FRAME, blockState)
            .rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing) + 180), Direction.UP).light(light).renderInto(ms, vc);
    }

    public static void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffers
    ) {
        BlockState blockState = context.state;
        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        VertexConsumer vc = buffers.getBuffer(RenderLayer.getCutoutMipped());
        SuperByteBuffer superBuffer = CachedBuffers.partial(AllPartialModels.ROLLER_WHEEL, blockState);
        float speed = !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            facing.getOpposite()
        ) ? context.getAnimationSpeed() : -context.getAnimationSpeed();
        if (context.contraption.stalled)
            speed = 0;

        superBuffer.transform(matrices.getModel()).translate(Vec3d.of(facing.getVector()).multiply(17 / 16f));
        HarvesterRenderer.transform(context.world, facing, superBuffer, speed, Vec3d.ZERO);

        MatrixStack viewProjection = matrices.getViewProjection();
        viewProjection.push();
        viewProjection.translate(0, -.25, 0);
        int contraptionWorldLight = WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos);
        superBuffer.translate(0, -.5, .5).rotateYDegrees(90).light(contraptionWorldLight).useLevelLight(context.world, matrices.getWorld())
            .renderInto(viewProjection, vc);
        viewProjection.pop();

        CachedBuffers.partial(AllPartialModels.ROLLER_FRAME, blockState).transform(matrices.getModel())
            .rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing) + 180), Direction.UP).light(contraptionWorldLight)
            .useLevelLight(context.world, matrices.getWorld()).renderInto(viewProjection, vc);
    }

}
