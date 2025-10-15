package com.zurrtum.create.client.content.contraptions.actors.harvester;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlock;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlockEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HarvesterRenderer implements BlockEntityRenderer<HarvesterBlockEntity, HarvesterRenderer.HarvesterRenderState> {
    private static final Vec3d PIVOT = new Vec3d(0, 6, 9);

    public HarvesterRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public HarvesterRenderState createRenderState() {
        return new HarvesterRenderState();
    }

    @Override
    public void updateRenderState(
        HarvesterBlockEntity be,
        HarvesterRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getCutoutMipped();
        state.model = CachedBuffers.partial(AllPartialModels.HARVESTER_BLADE, state.blockState);
        float originOffset = 1 / 16f;
        state.rotOffset = new Vec3d(0, PIVOT.y * originOffset, PIVOT.z * originOffset);
        float time = AnimationTickHolder.getRenderTime(be.getWorld()) / 20;
        state.angle = AngleHelper.rad((time * be.getAnimatedSpeed()) % 360);
        state.horizontalAngle = AngleHelper.rad(AngleHelper.horizontalAngle(state.blockState.get(HarvesterBlock.FACING)));
    }

    @Override
    public void render(HarvesterRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
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
            .renderInto(matrices.getViewProjection().peek(), buffers.getBuffer(RenderLayer.getCutoutMipped()));
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

    public static class HarvesterRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer model;
        public float angle;
        public Vec3d rotOffset;
        public float horizontalAngle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.rotateCentered(horizontalAngle, Direction.UP);
            model.translate(rotOffset.x, rotOffset.y, rotOffset.z);
            model.rotate(angle, Direction.WEST);
            model.translate(-rotOffset.x, -rotOffset.y, -rotOffset.z);
            model.light(lightmapCoordinates);
            model.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
