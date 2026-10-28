package com.zurrtum.create.client.content.trains.station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotItemState;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotOutputItemState;
import com.zurrtum.create.client.content.trains.station.StationRenderer.StationRenderState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlock;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class StationRenderer implements BlockEntityRenderer<StationBlockEntity, StationRenderState> {
    protected final ItemModelResolver itemModelManager;

    public StationRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public StationRenderState createRenderState() {
        return new StationRenderState();
    }

    @Override
    public void extractRenderState(
        StationBlockEntity be,
        StationRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        DepotBehaviour depotBehaviour = be.depotBehaviour;
        state.incoming = DepotRenderer.createIncomingStateList(depotBehaviour, itemModelManager, tickProgress, level);
        state.outputs = DepotRenderer.createOutputStateList(depotBehaviour, itemModelManager, level);
        TrackTargetingBehaviour<GlobalStation> target = be.edgePoint;
        BlockPos targetPosition = target.getGlobalPosition();
        BlockState trackState = level.getBlockState(targetPosition);
        Block block = trackState.getBlock();
        if (!(block instanceof ITrackBlock track)) {
            return;
        }
        GlobalStation station = be.getStation();
        boolean isAssembling = be.getBlockState().getValue(StationBlock.ASSEMBLING);
        if (!isAssembling || (station == null || station.getPresentTrain() != null) && !be.isVirtual()) {
            updateFlagState(
                level,
                be.flag.getValue(tickProgress) > 0.75f ? AllPartialModels.STATION_ON : AllPartialModels.STATION_OFF,
                be,
                state,
                tickProgress
            );
            TrackBlockRenderer renderer = AllTrackRenders.get(track);
            if (renderer != null) {
                state.block = renderer.getRenderState(
                    level,
                    new Vec3(
                        targetPosition.getX() - state.blockPos.getX(),
                        targetPosition.getY() - state.blockPos.getY(),
                        targetPosition.getZ() - state.blockPos.getZ()
                    ),
                    trackState,
                    targetPosition,
                    target.getTargetDirection(),
                    target.getTargetBezier(),
                    RenderedTrackOverlayType.STATION,
                    1
                );
            }
            return;
        }
        updateFlagState(level, AllPartialModels.STATION_ASSEMBLE, be, state, tickProgress);
        if (be.isVirtual() && be.bogeyLocations == null) {
            be.refreshAssemblyInfo();
        }
        TrackBlockRenderer renderer = AllTrackRenders.get(track);
        if (renderer == null) {
            return;
        }
        state.block = renderer.getAssemblyRenderState(
            be, new Vec3(
                targetPosition.getX() - state.blockPos.getX(),
                targetPosition.getY() - state.blockPos.getY(),
                targetPosition.getZ() - state.blockPos.getZ()
            ), level, targetPosition, trackState
        );
    }

    public void updateFlagState(
        Level level,
        PartialModel flag,
        StationBlockEntity be,
        StationRenderState state,
        float tickProgress
    ) {
        if (be.resolveFlagAngle()) {
            state.flag = CachedBuffers.partial(flag, be.getBlockState()).cardinalLighting(level)
                .light(state.lightCoords).extractRenderState();
            float value = be.flag.getValue(tickProgress);
            float progress = (float) Math.pow(Math.min(value * 5, 1), 2);
            if (be.flag.getChaseTarget() > 0 && !be.flag.settled() && progress == 1) {
                float wiggleProgress = (value - 0.2f) / 0.8f;
                progress += (float) (Math.sin(wiggleProgress * (2 * Mth.PI) * 4) / 8.0f / Math.max(
                    1,
                    8.0f * wiggleProgress
                ));
            }
            float nudge = 1 / 512.0f;
            state.flagYRot = be.flagYRot;
            boolean flipped = be.flagFlipped;
            state.flagOffsetZ = flipped ? 0.875f - nudge : 0.125f + nudge;
            state.flagXRot = (flipped ? 1 : -1) * (progress * 90 + 270);
            state.flagYRot2 = !flipped;
        }
    }

    @Override
    public void submit(
        StationRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.incoming != null || state.outputs != null) {
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
        if (state.flag != null) {
            matrices.pushPose();
            Pose pose = matrices.last();
            if (state.flagYRot != 0) {
                pose.rotateYCentered(Mth.DEG_TO_RAD * state.flagYRot);
            }
            pose.translate(0.001953125f, 0.59375f, state.flagOffsetZ);
            pose.rotate(Axis.XP, Mth.DEG_TO_RAD * state.flagXRot);
            if (state.flagYRot2) {
                pose.rotateYAround(Radians.DEG_180, 0.03125f, 0, 0);
            }
            state.flag.submit(matrices, queue);
            matrices.popPose();
        }
        if (state.block != null) {
            state.block.submit(matrices, queue);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96 * 2;
    }

    public static class StationRenderState extends BlockEntityRenderState {
        public DepotItemState @Nullable [] incoming;
        public @Nullable List<DepotOutputItemState> outputs;
        public @Nullable SuperByteBufferRenderState flag;
        public float flagYRot;
        public float flagOffsetZ;
        public float flagXRot;
        public boolean flagYRot2;
        public @Nullable TrackBlockRenderState block;
    }
}
