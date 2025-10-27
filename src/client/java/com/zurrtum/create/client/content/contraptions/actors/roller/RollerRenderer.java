package com.zurrtum.create.client.content.contraptions.actors.roller;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
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
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RollerRenderer extends SmartBlockEntityRenderer<RollerBlockEntity, RollerRenderer.RollerRenderState> {
    public RollerRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public RollerRenderState createRenderState() {
        return new RollerRenderState();
    }

    @Override
    public void updateRenderState(
        RollerBlockEntity be,
        RollerRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.layer = RenderLayer.getCutoutMipped();
        state.wheel = CachedBuffers.partial(AllPartialModels.ROLLER_WHEEL, state.blockState);
        Direction facing = state.blockState.get(RollerBlock.FACING);
        state.offset = Vec3d.of(facing.getVector()).multiply(17 / 16f).add(0, -0.25f, 0);
        float angle = AngleHelper.horizontalAngle(facing);
        state.wheelAngle = AngleHelper.rad(angle);
        World world = be.getWorld();
        float time = AnimationTickHolder.getRenderTime(world) / 20;
        state.rotate = AngleHelper.rad((time * be.getAnimatedSpeed()) % 360);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * 90;
        state.frame = CachedBuffers.partial(AllPartialModels.ROLLER_FRAME, state.blockState);
        state.frameAngle = AngleHelper.rad(angle + 180);
    }

    @Override
    public void render(RollerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
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
            .renderInto(viewProjection.peek(), vc);
        viewProjection.pop();

        CachedBuffers.partial(AllPartialModels.ROLLER_FRAME, blockState).transform(matrices.getModel())
            .rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing) + 180), Direction.UP).light(contraptionWorldLight)
            .useLevelLight(context.world, matrices.getWorld()).renderInto(viewProjection.peek(), vc);
    }

    public static class RollerRenderState extends SmartRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer wheel;
        public Vec3d offset;
        public float wheelAngle;
        public float rotate;
        public float yRot;
        public SuperByteBuffer frame;
        public float frameAngle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            wheel.translate(offset).rotateCentered(wheelAngle, Direction.UP).rotate(rotate, Direction.WEST).translate(0, -.5, .5).rotateY(yRot)
                .light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            frame.rotateCentered(frameAngle, Direction.UP).light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
