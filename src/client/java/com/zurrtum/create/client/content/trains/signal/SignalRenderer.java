package com.zurrtum.create.client.content.trains.signal;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.OverlayState;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalRenderer extends SafeBlockEntityRenderer<SignalBlockEntity> {

    public SignalRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(SignalBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockState blockState = be.getCachedState();
        SignalState signalState = be.getState();
        OverlayState overlayState = be.getOverlay();

        float renderTime = AnimationTickHolder.getRenderTime(be.getWorld());
        if (signalState.isRedLight(renderTime))
            CachedBuffers.partial(AllPartialModels.SIGNAL_ON, blockState).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE)
                .renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
        else
            CachedBuffers.partial(AllPartialModels.SIGNAL_OFF, blockState).light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));

        BlockPos pos = be.getPos();
        TrackTargetingBehaviour<SignalBoundary> target = be.edgePoint;
        BlockPos targetPosition = target.getGlobalPosition();
        World level = be.getWorld();
        BlockState trackState = level.getBlockState(targetPosition);
        Block block = trackState.getBlock();

        if (!(block instanceof ITrackBlock trackBlock))
            return;
        if (overlayState == OverlayState.SKIP)
            return;

        TrackBlockRenderer renderer = AllTrackRenders.get(trackBlock);
        if (renderer != null) {
            ms.push();
            TransformStack.of(ms).translate(targetPosition.subtract(pos));
            RenderedTrackOverlayType type = overlayState == OverlayState.DUAL ? RenderedTrackOverlayType.DUAL_SIGNAL : RenderedTrackOverlayType.SIGNAL;
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
