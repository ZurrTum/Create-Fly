package com.zurrtum.create.client.content.trains.observer;

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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TrackObserverRenderer extends SmartBlockEntityRenderer<TrackObserverBlockEntity, TrackObserverRenderer.TrackObserverRenderState> {
    public TrackObserverRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public TrackObserverRenderState createRenderState() {
        return new TrackObserverRenderState();
    }

    @Override
    public void updateRenderState(
        TrackObserverBlockEntity be,
        TrackObserverRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        World world = be.getWorld();
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
                world,
                new Vec3d(targetPosition.getX() - state.pos.getX(),
                    targetPosition.getY() - state.pos.getY(),
                    targetPosition.getZ() - state.pos.getZ()
                ),
                trackState,
                targetPosition,
                target.getTargetDirection(),
                target.getTargetBezier(),
                RenderedTrackOverlayType.OBSERVER,
                1
            );
        }
    }

    @Override
    public void render(TrackObserverRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.block != null) {
            state.block.render(matrices, queue);
        }
    }

    public static class TrackObserverRenderState extends SmartRenderState {
        public TrackBlockRenderState block;
    }
}
