package com.zurrtum.create.client.content.contraptions.elevator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.elevator.ElevatorPulleyRenderer.ElevatorPulleyRenderState;
import com.zurrtum.create.client.content.contraptions.pulley.AbstractPulleyRenderer;
import com.zurrtum.create.client.content.contraptions.pulley.PulleyRenderer;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlock;
import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class ElevatorPulleyRenderer implements BlockEntityRenderer<ElevatorPulleyBlockEntity, ElevatorPulleyRenderState> {
    private final Int2ObjectMap<SuperByteBufferRenderState> cache = new Int2ObjectOpenHashMap<>();

    public ElevatorPulleyRenderer(Context context) {
    }

    @Override
    public ElevatorPulleyRenderState createRenderState() {
        return new ElevatorPulleyRenderState();
    }

    @Override
    public void extractRenderState(
        ElevatorPulleyBlockEntity be,
        ElevatorPulleyRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Direction facing = state.blockState.getValue(ElevatorPulleyBlock.HORIZONTAL_FACING);
        Axis axis = facing.getClockWise().getAxis();
        state.shaft = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        state.axis = axis;
        state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
        float offset = PulleyRenderer.getBlockEntityOffset(tickProgress, be);
        boolean running = PulleyRenderer.isPulleyRunning(be);
        state.yRadians = Radians.horizontalPlus180(facing);
        BlockPos blockPos = state.blockPos;
        if (running || offset == 0) {
            state.magnetOffset = -offset;
            int magnetLight = LightCoordsUtil.getLightCoords(level, blockPos.below((int) offset));
            state.magnet = CachedBuffers.partial(AllPartialModels.ELEVATOR_MAGNET, state.blockState)
                .cardinalLighting(cardinalLighting).light(magnetLight).extractRenderState();
        }
        SuperByteBuffer rotatedCoil = CachedBuffers.partialFacing(
            AllPartialModels.ELEVATOR_COIL,
            state.blockState,
            facing
        ).cardinalLighting(cardinalLighting).light(state.lightCoords);
        if (offset == 0) {
            state.rotatedCoil = rotatedCoil.extractRenderState();
            return;
        }
        SpriteShiftEntry coilShift = AllSpriteShifts.ELEVATOR_COIL;
        SpriteShiftEntry halfShift = AllSpriteShifts.ELEVATOR_BELT;
        float coilScroll = AbstractPulleyRenderer.getCoilVScroll(coilShift, offset, 2);
        state.rotatedCoil = rotatedCoil.shiftUVScrolling(coilShift, coilScroll).extractRenderState();
        float f = offset % 1;
        float halfScroll;
        if (f < 0.25f || f > 0.75f) {
            halfScroll = getHalfShift(offset);
            float down = f > 0.75f ? f - 1 : f;
            state.halfRopeOffset = -down;
            int halfRopeLight = LightCoordsUtil.getLightCoords(level, blockPos.below((int) down));
            state.halfRope = CachedBuffers.partial(AllPartialModels.ELEVATOR_BELT_HALF, state.blockState)
                .cardinalLighting(cardinalLighting).light(halfRopeLight).shiftUVScrolling(halfShift, halfScroll)
                .extractRenderState();
            if (!running || offset <= 0.25f) {
                return;
            }
        } else {
            if (!running || offset <= 0.25f) {
                return;
            }
            halfScroll = getHalfShift(offset);
        }
        SuperByteBuffer rope = CachedBuffers.partial(AllPartialModels.ELEVATOR_BELT, state.blockState);
        int size = (int) Math.ceil(offset - 0.25f);
        SuperByteBufferRenderState[] ropes = new SuperByteBufferRenderState[size];
        state.ropeOffset = -offset - 1;
        for (int i = 0, down = (int) offset; i < size; i++, down--) {
            ropes[i] = cache.computeIfAbsent(
                LightCoordsUtil.getLightCoords(level, blockPos.below(down)),
                l -> rope.cardinalLighting(cardinalLighting).light(l).shiftUVScrolling(halfShift, halfScroll)
                    .extractRenderState()
            );
        }
        cache.clear();
        state.ropes = ropes;
    }

    @Override
    public void submit(
        ElevatorPulleyRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
            state.shaft.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.shaft.submit(matrices, queue);
        }
        state.rotatedCoil.submit(matrices, queue);
        if (state.yRadians != 0) {
            matrices.rotateYCentered(state.yRadians);
        }
        if (state.magnet != null) {
            matrices.pushPose();
            matrices.translate(0, state.magnetOffset, 0);
            state.magnet.submit(matrices, queue);
            matrices.popPose();
        }
        if (state.halfRope != null) {
            matrices.pushPose();
            matrices.translate(0, state.halfRopeOffset, 0);
            state.halfRope.submit(matrices, queue);
            matrices.popPose();
        }
        if (state.ropes != null) {
            Pose pose = matrices.last();
            pose.translate(0, state.ropeOffset, 0);
            for (SuperByteBufferRenderState rope : state.ropes) {
                pose.translate(0, 1, 0);
                rope.submit(matrices, queue);
            }
        }
    }

    private static float getHalfShift(float offset) {
        double beltScroll = (-(offset + 0.5) - Math.floor(-(offset + 0.5))) / 2;
        TextureAtlasSprite target = AllSpriteShifts.ELEVATOR_BELT.getTarget();
        float spriteSize = target.getV1() - target.getV0();
        return (float) beltScroll * spriteSize;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class ElevatorPulleyRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaft;
        public @UnknownNullability Axis axis;
        public float angle;
        public float yRadians;
        public float magnetOffset;
        public float halfRopeOffset;
        public float ropeOffset;
        public @Nullable SuperByteBufferRenderState magnet;
        public @UnknownNullability SuperByteBufferRenderState rotatedCoil;
        public @Nullable SuperByteBufferRenderState halfRope;
        public SuperByteBufferRenderState @Nullable [] ropes;
    }
}
