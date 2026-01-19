package com.zurrtum.create.client.content.trains.signal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SignalRenderer implements BlockEntityRenderer<SignalBlockEntity, SignalRenderer.SignalRenderState> {
    public SignalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SignalRenderState createRenderState() {
        return new SignalRenderState();
    }

    @Override
    public void extractRenderState(
        SignalBlockEntity be,
        SignalRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        Level world = be.getLevel();
        if (VisualizationManager.supportsVisualization(world)) {
            return;
        }
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        state.layer = RenderTypes.solidMovingBlock();
        float renderTime = AnimationTickHolder.getRenderTime(world);
        if (be.getState().isRedLight(renderTime)) {
            state.model = CachedBuffers.partial(AllPartialModels.SIGNAL_ON, state.blockState);
            state.lightCoords = LightCoordsUtil.MAX_SMOOTH_LIGHT_LEVEL;
        } else {
            state.model = CachedBuffers.partial(AllPartialModels.SIGNAL_OFF, state.blockState);
            state.lightCoords = world != null ? LevelRenderer.getLightCoords(world, state.blockPos) : LightCoordsUtil.FULL_BRIGHT;
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
            RenderedTrackOverlayType type = overlayState == OverlayState.DUAL ? RenderedTrackOverlayType.DUAL_SIGNAL : RenderedTrackOverlayType.SIGNAL;
            state.block = renderer.getRenderState(
                world, new Vec3(
                    targetPosition.getX() - state.blockPos.getX(),
                    targetPosition.getY() - state.blockPos.getY(),
                    targetPosition.getZ() - state.blockPos.getZ()
                ), trackState, targetPosition, target.getTargetDirection(), target.getTargetBezier(), type, 1
            );
        }
    }

    @Override
    public void submit(SignalRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
        if (state.block != null) {
            state.block.render(matrices, queue);
        }
    }

    public static class SignalRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer model;
        TrackBlockRenderState block;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
