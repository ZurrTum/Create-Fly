package com.zurrtum.create.client.content.trains.observer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.trains.observer.TrackObserver;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TrackObserverRenderer extends SmartBlockEntityRenderer<TrackObserverBlockEntity, TrackObserverRenderer.TrackObserverRenderState> {
    public TrackObserverRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TrackObserverRenderState createRenderState() {
        return new TrackObserverRenderState();
    }

    @Override
    public void extractRenderState(
        TrackObserverBlockEntity be,
        TrackObserverRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level world = be.getLevel();
        if (VisualizationManager.supportsVisualization(world)) {
            return;
        }
        TrackTargetingBehaviour<TrackObserver> target = be.edgePoint;
        BlockPos targetPosition = target.getGlobalPosition();
        BlockState trackState = world.getBlockState(targetPosition);
        Block block = trackState.getBlock();
        if (!(block instanceof ITrackBlock track)) {
            return;
        }
        TrackBlockRenderer renderer = AllTrackRenders.get(track);
        if (renderer != null) {
            state.block = renderer.getRenderState(
                world, new Vec3(
                    targetPosition.getX() - state.blockPos.getX(),
                    targetPosition.getY() - state.blockPos.getY(),
                    targetPosition.getZ() - state.blockPos.getZ()
                ), trackState, targetPosition, target.getTargetDirection(), target.getTargetBezier(), RenderedTrackOverlayType.OBSERVER, 1
            );
        }
    }

    @Override
    public void submit(TrackObserverRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.block != null) {
            state.block.render(matrices, queue);
        }
    }

    public static class TrackObserverRenderState extends SmartRenderState {
        public TrackBlockRenderState block;
    }
}
