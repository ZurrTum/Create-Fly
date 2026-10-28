package com.zurrtum.create.client.content.kinetics.gauge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.gauge.GaugeRenderer.GaugeRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlock;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlock.Type;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlockEntity;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class GaugeRenderer implements BlockEntityRenderer<GaugeBlockEntity, GaugeRenderState> {
    private final PartialModel model;

    public static GaugeRenderer speed(Context context) {
        return new GaugeRenderer(Type.SPEED);
    }

    public static GaugeRenderer stress(Context context) {
        return new GaugeRenderer(Type.STRESS);
    }

    protected GaugeRenderer(Type type) {
        model = type == Type.SPEED ? AllPartialModels.GAUGE_HEAD_SPEED : AllPartialModels.GAUGE_HEAD_STRESS;
    }

    @Override
    public GaugeRenderState createRenderState() {
        return new GaugeRenderState();
    }

    @Override
    public void extractRenderState(
        GaugeBlockEntity be,
        GaugeRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Axis axis = getRotationAxisOf(state.blockState);
        state.model = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        state.axis = axis;
        state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
        BlockPos pos = state.blockPos;
        BlockState gaugeState = state.blockState;
        GaugeBlock block = (GaugeBlock) gaugeState.getBlock();
        FloatArrayList angles = new FloatArrayList(2);
        for (Direction facing : Iterate.directions) {
            if (block.shouldRenderHeadOnFace(level, pos, gaugeState, facing)) {
                angles.add(-facing.toYRot() - 90);
            }
        }
        if (angles.isEmpty()) {
            return;
        }
        state.angles = angles;
        state.head = CachedBuffers.partial(model, gaugeState).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).extractRenderState();
        state.dial = CachedBuffers.partial(AllPartialModels.GAUGE_DIAL, gaugeState).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).extractRenderState();
        float progress = Mth.lerp(tickProgress, be.prevDialState, be.dialState);
        if (progress != 0) {
            state.xRadians = -Radians.DEG_90 * progress;
        }
    }

    @Override
    public void submit(
        GaugeRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
            state.model.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.model.submit(matrices, queue);
        }
        if (state.angles != null) {
            if (state.xRadians != 0) {
                for (float angle : state.angles) {
                    matrices.pushPose();
                    Pose pose = matrices.last();
                    if (angle != 0) {
                        pose.rotateYCentered(Mth.DEG_TO_RAD * angle);
                    }
                    state.head.submit(matrices, queue);
                    pose.rotateXAround(state.xRadians, 0, 0.359375f, 0.359375f);
                    state.dial.submit(matrices, queue);
                    matrices.popPose();
                }
            } else {
                for (float angle : state.angles) {
                    if (angle != 0) {
                        matrices.pushPose();
                        matrices.rotateYCentered(Mth.DEG_TO_RAD * angle);
                        state.head.submit(matrices, queue);
                        state.dial.submit(matrices, queue);
                        matrices.popPose();
                    } else {
                        state.head.submit(matrices, queue);
                        state.dial.submit(matrices, queue);
                    }
                }
            }
        }
    }

    public static class GaugeRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @UnknownNullability SuperByteBufferRenderState head;
        public @UnknownNullability SuperByteBufferRenderState dial;
        public @UnknownNullability Axis axis;
        public float angle;
        public @UnknownNullability FloatArrayList angles;
        public float xRadians;
    }
}
