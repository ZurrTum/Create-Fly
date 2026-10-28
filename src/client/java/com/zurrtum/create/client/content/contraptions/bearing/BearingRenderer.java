package com.zurrtum.create.client.content.contraptions.bearing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.bearing.BearingRenderer.BearingRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.bearing.IBearingBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getDegreesWithoutBeOffset;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getTintColor;

public class BearingRenderer<T extends KineticBlockEntity & IBearingBlockEntity> implements BlockEntityRenderer<T, BearingRenderState> {
    public BearingRenderer(Context context) {
    }

    @Override
    public BearingRenderState createRenderState() {
        return new BearingRenderState();
    }

    @Override
    public void extractRenderState(
        T be,
        BearingRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        int lightCoords = state.lightCoords;
        BlockState blockState = state.blockState;
        Direction facing = blockState.getValue(BlockStateProperties.FACING);
        Axis axis = facing.getAxis();
        int color = getTintColor(be);
        Direction opposite = facing.getOpposite();
        state.axis = axis;
        state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
        state.shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, opposite)
            .cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
        state.topAngle = be.getInterpolatedAngle(tickProgress - 1);
        state.xRadians = Radians.negatedVerticalPlus90(facing);
        state.yRadians = Radians.horizontal(opposite);
        state.top = CachedBuffers.partial(
            be.isWoodenTop() ? AllPartialModels.BEARING_TOP_WOODEN : AllPartialModels.BEARING_TOP,
            blockState
        ).cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
    }

    @Override
    public void submit(
        BearingRenderState state,
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
        if (state.topAngle != 0) {
            pose.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.topAngle);
        }
        if (state.yRadians != 0) {
            pose.rotateYCentered(state.yRadians);
        }
        if (state.xRadians != 0) {
            pose.rotateXCentered(state.xRadians);
        }
        state.top.submit(matrices, queue);
    }

    public static class BearingRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaft;
        public @UnknownNullability SuperByteBufferRenderState top;
        public @UnknownNullability Axis axis;
        public float angle;
        public float topAngle;
        public float yRadians;
        public float xRadians;
    }
}
