package com.zurrtum.create.client.content.trains.observer;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import com.zurrtum.create.content.trains.observer.TrackObserver;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TrackObserverVisual extends AbstractBlockEntityVisual<TrackObserverBlockEntity> implements SimpleTickableVisual {
    private final TransformedInstance overlay;
    private BlockPos oldTargetPos;

    public TrackObserverVisual(VisualizationContext ctx, TrackObserverBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        overlay = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.TRACK_OBSERVER_OVERLAY))
            .createInstance();

        setupVisual();
    }

    @Override
    public void tick(Context context) {
        setupVisual();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(overlay);
    }

    @Override
    protected void _delete() {
        overlay.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(overlay);
    }

    private void setupVisual() {
        TrackTargetingBehaviour<TrackObserver> target = blockEntity.edgePoint;
        BlockPos targetPosition = target.getGlobalPosition();
        Level level = blockEntity.getLevel();
        BlockState trackState = level.getBlockState(targetPosition);
        Block block = trackState.getBlock();

        if (!(block instanceof ITrackBlock trackBlock)) {
            overlay.setZeroTransform().setChanged();
            return;
        }

        if (!targetPosition.equals(oldTargetPos)) {
            oldTargetPos = targetPosition;

            overlay.setIdentityTransform().translate(targetPosition.subtract(renderOrigin()));

            TrackBlockRenderer renderer = AllTrackRenders.get(trackBlock);
            if (renderer != null) {
                RenderedTrackOverlayType type = RenderedTrackOverlayType.OBSERVER;
                renderer.prepareTrackOverlay(overlay, level, targetPosition, trackState, target.getTargetBezier(), target.getTargetDirection(), type);
            }

            overlay.setChanged();
        }
    }
}
