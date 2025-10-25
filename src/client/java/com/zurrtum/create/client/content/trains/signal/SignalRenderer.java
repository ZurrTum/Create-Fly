package com.zurrtum.create.client.content.trains.signal;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.OverlayState;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SignalRenderer implements BlockEntityRenderer<SignalBlockEntity, SignalRenderer.SignalRenderState> {
    public SignalRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public SignalRenderState createRenderState() {
        return new SignalRenderState();
    }

    @Override
    public void updateRenderState(
        SignalBlockEntity be,
        SignalRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        World world = be.getWorld();
        if (VisualizationManager.supportsVisualization(world)) {
            return;
        }
        state.pos = be.getPos();
        state.blockState = be.getCachedState();
        state.type = be.getType();
        state.layer = RenderLayer.getSolid();
        float renderTime = AnimationTickHolder.getRenderTime(world);
        if (be.getState().isRedLight(renderTime)) {
            state.model = CachedBuffers.partial(AllPartialModels.SIGNAL_ON, state.blockState);
            state.lightmapCoordinates = LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE;
        } else {
            state.model = CachedBuffers.partial(AllPartialModels.SIGNAL_OFF, state.blockState);
            state.lightmapCoordinates = world != null ? WorldRenderer.getLightmapCoordinates(
                world,
                state.pos
            ) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
        }
        TrackTargetingBehaviour<SignalBoundary> target = be.edgePoint;
        BlockPos targetPosition = target.getGlobalPosition();
        BlockState trackState = world.getBlockState(targetPosition);
        Block block = trackState.getBlock();
        if (!(block instanceof ITrackBlock trackBlock)) {
            return;
        }
        OverlayState overlayState = be.getOverlay();
        if (overlayState == OverlayState.SKIP) {
            return;
        }
        TrackBlockRenderer renderer = AllTrackRenders.get(trackBlock);
        if (renderer != null) {
            state.offset = targetPosition.subtract(state.pos);
            RenderedTrackOverlayType type = overlayState == OverlayState.DUAL ? RenderedTrackOverlayType.DUAL_SIGNAL : RenderedTrackOverlayType.SIGNAL;
            state.block = renderer.getRenderState(world, trackState, targetPosition, target.getTargetDirection(), target.getTargetBezier(), type, 1);
        }
    }

    @Override
    public void render(SignalRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
        if (state.block != null) {
            BlockPos offset = state.offset;
            matrices.translate(offset.getX(), offset.getY(), offset.getZ());
            state.block.render(matrices, queue);
        }
    }

    public static class SignalRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer model;
        public BlockPos offset;
        TrackBlockRenderState block;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
