package com.zurrtum.create.client.content.logistics.depot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotItemState;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotOutputItemState;
import com.zurrtum.create.client.content.logistics.depot.EjectorRenderer.EjectorRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.logistics.depot.EjectorBlock;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class EjectorRenderer implements BlockEntityRenderer<EjectorBlockEntity, EjectorRenderState> {
    static final Vec3 pivot = VecHelper.voxelSpace(0, 11.25, 0.75);
    protected final ItemModelResolver itemModelManager;

    public EjectorRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public EjectorRenderState createRenderState() {
        return new EjectorRenderState();
    }

    @Override
    public void extractRenderState(
        EjectorBlockEntity be,
        EjectorRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        if (VisualizationManager.supportsVisualization(level)) {
            DepotBehaviour behaviour = be.getBehaviour(DepotBehaviour.TYPE);
            if (behaviour == null || behaviour.isEmpty()) {
                return;
            }
            state.incoming = DepotRenderer.createIncomingStateList(behaviour, itemModelManager, tickProgress, level);
            state.outputs = DepotRenderer.createOutputStateList(behaviour, itemModelManager, level);
            if (state.incoming != null || state.outputs != null) {
                SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
                state.lidProgress = be.getLidProgress(tickProgress);
                Direction direction = state.blockState.getValue(EjectorBlock.HORIZONTAL_FACING);
                state.yRadians = Radians.horizontalPlus180(direction);
            }
        } else {
            SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
            CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
            Direction direction = state.blockState.getValue(EjectorBlock.HORIZONTAL_FACING);
            Axis axis = direction.getClockWise().getAxis();
            state.model = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
                .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
            state.axis = axis;
            state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
            state.top = CachedBuffers.partial(AllPartialModels.EJECTOR_TOP, state.blockState)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            state.lidProgress = be.getLidProgress(tickProgress);
            state.yRadians = Radians.horizontalPlus180(direction);
            DepotBehaviour behaviour = be.getBehaviour(DepotBehaviour.TYPE);
            if (behaviour == null || behaviour.isEmpty()) {
                return;
            }
            state.incoming = DepotRenderer.createIncomingStateList(behaviour, itemModelManager, tickProgress, level);
            state.outputs = DepotRenderer.createOutputStateList(behaviour, itemModelManager, level);
        }
    }

    @Override
    public void submit(
        EjectorRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.model != null) {
            if (state.angle != 0) {
                matrices.pushPose();
                matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
                state.model.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.model.submit(matrices, queue);
            }
        }
        Pose pose = matrices.last();
        if (state.yRadians != 0) {
            pose.rotateYCentered(state.yRadians);
        }
        if (state.lidProgress != 0) {
            pose.rotateXAround(state.lidProgress * -Radians.DEG_70, 0, (float) pivot.y, (float) pivot.z);
        }
        if (state.top != null) {
            state.top.submit(matrices, queue);
        }
        if (state.incoming != null || state.outputs != null) {
            if (state.yRadians != 0) {
                pose.rotateYCentered(-state.yRadians);
            }
            DepotRenderer.renderItemsOf(
                state.incoming,
                state.outputs,
                state.blockPos,
                cameraState.pos,
                queue,
                matrices,
                state.lightCoords
            );
        }
    }

    static <T extends Affine<T>> void applyLidAngle(KineticBlockEntity be, float angle, T tr) {
        tr.rotateYCentered(Radians.horizontalPlus180(be.getBlockState().getValue(EjectorBlock.HORIZONTAL_FACING)))
            .translate(pivot).rotateXDegrees(-angle).translateBack(pivot);
    }

    public static class EjectorRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState model;
        public @UnknownNullability Axis axis;
        public float angle;
        public float lidProgress;
        public float yRadians;
        public @Nullable SuperByteBufferRenderState top;
        public DepotItemState @Nullable [] incoming;
        public @Nullable List<DepotOutputItemState> outputs;
    }
}
