package com.zurrtum.create.client.content.kinetics.clock;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.CuckooClockAnimationBehaviour;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlock;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity.Animation;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CuckooClockRenderer extends KineticBlockEntityRenderer<CuckooClockBlockEntity, CuckooClockRenderer.CuckooClockRenderState> {
    public CuckooClockRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public CuckooClockRenderState createRenderState() {
        return new CuckooClockRenderState();
    }

    @Override
    public void updateRenderState(
        CuckooClockBlockEntity be,
        CuckooClockRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.support) {
            state.pos = be.getPos();
            state.blockState = be.getCachedState();
            state.type = be.getType();
            World world = be.getWorld();
            state.lightmapCoordinates = world != null ? WorldRenderer.getLightmapCoordinates(
                world,
                state.pos
            ) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
            state.layer = RenderLayer.getSolid();
            state.facing = state.blockState.get(CuckooClockBlock.HORIZONTAL_FACING);
        }
        state.hourHand = CachedBuffers.partial(AllPartialModels.CUCKOO_HOUR_HAND, state.blockState);
        state.minuteHand = CachedBuffers.partial(AllPartialModels.CUCKOO_MINUTE_HAND, state.blockState);
        CuckooClockAnimationBehaviour behaviour = (CuckooClockAnimationBehaviour) be.getBehaviour(AnimationBehaviour.TYPE);
        state.angle = AngleHelper.rad(AngleHelper.horizontalAngle(state.facing.rotateYCounterclockwise()));
        state.hourAngle = AngleHelper.rad(behaviour.hourHand.getValue(tickProgress));
        state.minuteAngle = AngleHelper.rad(behaviour.minuteHand.getValue(tickProgress));
        state.leftDoor = CachedBuffers.partial(AllPartialModels.CUCKOO_LEFT_DOOR, state.blockState);
        state.rightDoor = CachedBuffers.partial(AllPartialModels.CUCKOO_RIGHT_DOOR, state.blockState);
        float angle = 0;
        if (be.animationType != null) {
            float value = be.animationProgress.getValue(tickProgress);
            int step = be.animationType == Animation.SURPRISE ? 3 : 15;
            for (int phase = 30; phase <= 60; phase += step) {
                float local = value - phase;
                if (local < -step / 3)
                    continue;
                else if (local < 0)
                    angle = MathHelper.lerp(((value - (phase - 5)) / 5), 0, 135);
                else if (local < step / 3)
                    angle = 135;
                else if (local < 2 * step / 3)
                    angle = MathHelper.lerp(((value - (phase + 5)) / 5), 135, 0);
            }
        }
        state.doorAngle = AngleHelper.rad(angle);
        if (be.animationType != Animation.NONE) {
            PartialModel partialModel = (be.animationType == Animation.PIG ? AllPartialModels.CUCKOO_PIG : AllPartialModels.CUCKOO_CREEPER);
            state.figure = CachedBuffers.partial(partialModel, state.blockState);
            state.offset = -(angle / 135) * 1 / 2f + 10 / 16f;
        }
    }

    @Override
    public void updateBaseRenderState(
        CuckooClockBlockEntity be,
        CuckooClockRenderState state,
        World world,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateBaseRenderState(be, state, world, crumblingOverlay);
        state.facing = state.blockState.get(CuckooClockBlock.HORIZONTAL_FACING);
    }

    @Override
    public void render(CuckooClockRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    @Override
    protected RenderLayer getRenderType(CuckooClockBlockEntity be, BlockState state) {
        return RenderLayer.getSolid();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(CuckooClockBlockEntity be, CuckooClockRenderState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, state.facing.getOpposite());
    }

    public static class CuckooClockRenderState extends KineticRenderState {
        public Direction facing;
        public SuperByteBuffer hourHand;
        public SuperByteBuffer minuteHand;
        public float angle;
        public float hourAngle;
        public float minuteAngle;
        public SuperByteBuffer leftDoor;
        public SuperByteBuffer rightDoor;
        public float doorAngle;
        public SuperByteBuffer figure;
        public float offset;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            if (model != null) {
                super.render(matricesEntry, vertexConsumer);
            }
            hourHand.rotateCentered(angle, Direction.UP).translate(0.125f, 0.375f, 0.5f).rotate(hourAngle, Direction.EAST)
                .translate(-0.125f, -0.375f, -0.5f).light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            minuteHand.rotateCentered(angle, Direction.UP).translate(0.125f, 0.375f, 0.5f).rotate(minuteAngle, Direction.EAST)
                .translate(-0.125f, -0.375f, -0.5f).light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            leftDoor.rotateCentered(angle, Direction.UP).translate(0.125f, 0, 0.375f).rotate(-doorAngle, Direction.UP).translate(-0.125f, 0, -0.375f)
                .light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            rightDoor.rotateCentered(angle, Direction.UP).translate(0.125f, 0, 0.625f).rotate(doorAngle, Direction.UP).translate(-0.125f, 0, -0.625f)
                .light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            if (figure != null) {
                figure.rotateCentered(angle, Direction.UP).translate(offset, 0, 0).light(lightmapCoordinates)
                    .renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
