package com.zurrtum.create.client.content.trains.observer;

import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.trains.observer.TrackObserver;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TrackObserverRenderer extends SmartBlockEntityRenderer<TrackObserverBlockEntity> {

    public TrackObserverRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        TrackObserverBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockPos pos = be.getPos();

        TrackTargetingBehaviour<TrackObserver> target = be.edgePoint;
        BlockPos targetPosition = target.getGlobalPosition();
        World level = be.getWorld();
        BlockState trackState = level.getBlockState(targetPosition);
        Block block = trackState.getBlock();

        if (!(block instanceof ITrackBlock track))
            return;

        TrackBlockRenderer renderer = AllTrackRenders.get(track);
        if (renderer != null) {
            ms.push();
            TransformStack.of(ms).translate(targetPosition.subtract(pos));
            RenderedTrackOverlayType type = RenderedTrackOverlayType.OBSERVER;
            renderer.render(
                level,
                trackState,
                targetPosition,
                target.getTargetDirection(),
                target.getTargetBezier(),
                ms,
                buffer,
                light,
                overlay,
                type,
                1
            );
            ms.pop();
        }
    }

}
