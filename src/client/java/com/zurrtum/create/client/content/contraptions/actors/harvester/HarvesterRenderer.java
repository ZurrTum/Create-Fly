package com.zurrtum.create.client.content.contraptions.actors.harvester;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlock;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlockEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HarvesterRenderer extends SafeBlockEntityRenderer<HarvesterBlockEntity> {

    private static final Vec3d PIVOT = new Vec3d(0, 6, 9);

    public HarvesterRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(HarvesterBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        BlockState blockState = be.getCachedState();
        SuperByteBuffer superBuffer = CachedBuffers.partial(AllPartialModels.HARVESTER_BLADE, blockState);
        transform(be.getWorld(), blockState.get(HarvesterBlock.FACING), superBuffer, be.getAnimatedSpeed(), PIVOT);
        superBuffer.light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getCutoutMipped()));
    }

    public static void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffers
    ) {
        BlockState blockState = context.state;
        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        SuperByteBuffer superBuffer = CachedBuffers.partial(AllPartialModels.HARVESTER_BLADE, blockState);
        float speed = !VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()) ? context.getAnimationSpeed() : 0;
        if (context.contraption.stalled)
            speed = 0;

        superBuffer.transform(matrices.getModel());
        transform(context.world, facing, superBuffer, speed, PIVOT);

        superBuffer.light(WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos)).useLevelLight(context.world, matrices.getWorld())
            .renderInto(matrices.getViewProjection(), buffers.getBuffer(RenderLayer.getCutoutMipped()));
    }

    public static void transform(World world, Direction facing, SuperByteBuffer superBuffer, float speed, Vec3d pivot) {
        float originOffset = 1 / 16f;
        Vec3d rotOffset = new Vec3d(0, pivot.y * originOffset, pivot.z * originOffset);
        float time = AnimationTickHolder.getRenderTime(world) / 20;
        float angle = (time * speed) % 360;

        superBuffer.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing)), Direction.UP)
            .translate(rotOffset.x, rotOffset.y, rotOffset.z).rotate(AngleHelper.rad(angle), Direction.WEST)
            .translate(-rotOffset.x, -rotOffset.y, -rotOffset.z);
    }
}
