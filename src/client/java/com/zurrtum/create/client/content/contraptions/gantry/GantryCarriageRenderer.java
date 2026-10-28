package com.zurrtum.create.client.content.contraptions.gantry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.gantry.GantryCarriageRenderer.GantryCarriageRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlock;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual.rotationOffset;

public class GantryCarriageRenderer implements BlockEntityRenderer<GantryCarriageBlockEntity, GantryCarriageRenderState> {
    public GantryCarriageRenderer(Context context) {
    }

    @Override
    public GantryCarriageRenderState createRenderState() {
        return new GantryCarriageRenderState();
    }

    @Override
    public void extractRenderState(
        GantryCarriageBlockEntity be,
        GantryCarriageRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Direction.Axis rotationAxis = getRotationAxisOf(state.blockState);
        state.shaft = CachedBuffers.block(KINETIC_BLOCK, shaft(rotationAxis)).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        float progress = getProgress(be, level);
        float offset = rotationOffset(state.blockState, rotationAxis, state.blockPos);
        state.axis = rotationAxis;
        state.angle = (progress + offset) % 360;
        Direction facing = state.blockState.getValue(GantryCarriageBlock.FACING);
        if (facing.getAxisDirection() != AxisDirection.POSITIVE) {
            offset = rotationOffset(state.blockState, rotationAxis, state.blockPos.relative(facing.getOpposite()));
        }
        Direction.Axis facingAxis = facing.getAxis();
        Direction.Axis gantryAxis = Direction.Axis.X;
        for (Direction.Axis axis : Iterate.axes) {
            if (axis != rotationAxis && axis != facingAxis) {
                gantryAxis = axis;
            }
        }
        float angleForBE = -(progress / 2 + offset) % 360;
        if (gantryAxis == Direction.Axis.X) {
            if (facing == Direction.UP) {
                angleForBE = -angleForBE;
            }
        } else if (gantryAxis == Direction.Axis.Y) {
            if (facing == Direction.NORTH || facing == Direction.EAST) {
                angleForBE = -angleForBE;
            }
        }
        state.xRot2 = angleForBE;
        state.yRadians = Radians.horizontal(facing);
        if (facing != Direction.UP) {
            state.xRadians = facing == Direction.DOWN ? Radians.DEG_180 : Radians.DEG_90;
        }
        state.alongFirst = state.blockState.getValue(GantryCarriageBlock.AXIS_ALONG_FIRST_COORDINATE) == (facingAxis == Direction.Axis.X);
        state.cogs = CachedBuffers.partial(AllPartialModels.GANTRY_COGS, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
    }

    @Override
    public void submit(
        GantryCarriageRenderState state,
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
        if (state.alongFirst) {
            pose.rotate(Axis.YP, Radians.DEG_90);
        }
        if (state.xRot2 != 0) {
            pose.rotateXAround(Mth.DEG_TO_RAD * state.xRot2, 0, -0.5625f, 0);
        }
        pose.translate(-0.5f, -0.5f, -0.5f);
        state.cogs.submit(matrices, queue);
    }

    public static float getAngleForBE(KineticBlockEntity be, final BlockPos pos, Direction.Axis axis) {
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float offset = getRotationOffsetForPosition(be, pos, axis);
        return (time * be.getSpeed() * 3.0f / 20 + offset) % 360;
    }

    public static class GantryCarriageRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaft;
        public @UnknownNullability SuperByteBufferRenderState cogs;
        public Direction.@UnknownNullability Axis axis;
        public float angle;
        public float yRadians;
        public float xRadians;
        public boolean alongFirst;
        public float xRot2;
    }
}
