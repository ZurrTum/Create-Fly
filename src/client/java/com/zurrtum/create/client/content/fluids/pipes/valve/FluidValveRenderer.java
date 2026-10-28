package com.zurrtum.create.client.content.fluids.pipes.valve;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.fluids.pipes.valve.FluidValveRenderer.FluidValveRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlock;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class FluidValveRenderer implements BlockEntityRenderer<FluidValveBlockEntity, FluidValveRenderState> {
    public FluidValveRenderer(Context context) {
    }

    @Override
    public FluidValveRenderState createRenderState() {
        return new FluidValveRenderState();
    }

    @Override
    public void extractRenderState(
        FluidValveBlockEntity be,
        FluidValveRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Direction.Axis shaftAxis = getRotationAxisOf(state.blockState);
        state.shaft = CachedBuffers.block(KINETIC_BLOCK, shaft(shaftAxis)).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        state.axis = shaftAxis;
        state.angle = getDegreesWithoutBeOffset(shaftAxis, be, state, level);
        Direction facing = state.blockState.getValue(FluidValveBlock.FACING);
        state.yRadians = Radians.horizontal(facing);
        if (facing != Direction.UP) {
            state.xRadians = facing == Direction.DOWN ? Radians.DEG_180 : Radians.DEG_90;
        }
        Direction.Axis pipeAxis = FluidValveBlock.getPipeAxis(state.blockState);
        float pointerRotation = Mth.lerp(be.pointer.getValue(tickProgress), 0, -90);
        if (pipeAxis.isHorizontal() && shaftAxis == Direction.Axis.X || pipeAxis.isVertical()) {
            state.yRot2 = 90 + pointerRotation;
        } else {
            state.yRot2 = pointerRotation;
        }
        state.pointer = CachedBuffers.partial(AllPartialModels.FLUID_VALVE_POINTER, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
    }

    @Override
    public void submit(
        FluidValveRenderState state,
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
        Pose pose = matrices.last();
        pose.translate(0.5f, 0.5f, 0.5f);
        if (state.yRadians != 0) {
            pose.rotate(Axis.YP, state.yRadians);
        }
        if (state.xRadians != 0) {
            pose.rotate(Axis.XP, state.xRadians);
        }
        if (state.yRot2 != 0) {
            pose.rotate(Axis.YP, Mth.DEG_TO_RAD * state.yRot2);
        }
        matrices.translate(-0.5f, -0.5f, -0.5f);
        state.pointer.submit(matrices, queue);
    }

    public static class FluidValveRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaft;
        public Direction.@UnknownNullability Axis axis;
        public float angle;
        public @UnknownNullability SuperByteBufferRenderState pointer;
        public float yRadians;
        public float xRadians;
        public float yRot2;
    }
}
