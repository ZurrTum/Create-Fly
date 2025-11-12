package com.zurrtum.create.client.content.trains.station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotItemState;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotOutputItemState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlock;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StationRenderer implements BlockEntityRenderer<StationBlockEntity, StationRenderer.StationRenderState> {
    protected final ItemModelResolver itemModelManager;

    public StationRenderer(BlockEntityRendererProvider.Context context) {
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        state.blockPos = be.getBlockPos();
        state.blockEntityType = be.getType();
        Level world = be.getLevel();
        state.lightCoords = world != null ? LevelRenderer.getLightColor(world, state.blockPos) : LightTexture.FULL_BRIGHT;
        DepotBehaviour depotBehaviour = be.depotBehaviour;
        state.incoming = DepotRenderer.createIncomingStateList(depotBehaviour, itemModelManager, tickProgress, world);
        state.outputs = DepotRenderer.createOutputStateList(depotBehaviour, itemModelManager, world);
        TrackTargetingBehaviour<GlobalStation> target = be.edgePoint;
        BlockPos targetPosition = target.getGlobalPosition();
        BlockState trackState = world.getBlockState(targetPosition);
        Block block = trackState.getBlock();
        if (!(block instanceof ITrackBlock track)) {
            return;
        }
        GlobalStation station = be.getStation();
        boolean isAssembling = be.getBlockState().getValue(StationBlock.ASSEMBLING);
        if (!isAssembling || (station == null || station.getPresentTrain() != null) && !be.isVirtual()) {
            updateFlagState(
                be.flag.getValue(tickProgress) > 0.75f ? AllPartialModels.STATION_ON : AllPartialModels.STATION_OFF,
                be,
                state,
                tickProgress
            );
            TrackBlockRenderer renderer = AllTrackRenders.get(track);
            if (renderer != null) {
                state.block = renderer.getRenderState(
                    world, new Vec3(
                        targetPosition.getX() - state.blockPos.getX(),
                        targetPosition.getY() - state.blockPos.getY(),
                        targetPosition.getZ() - state.blockPos.getZ()
                    ), trackState, targetPosition, target.getTargetDirection(), target.getTargetBezier(), RenderedTrackOverlayType.STATION, 1
                );
            }
            return;
        }
        updateFlagState(AllPartialModels.STATION_ASSEMBLE, be, state, tickProgress);
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
            ), world, targetPosition, trackState
        );
    }

    public void updateFlagState(PartialModel flag, StationBlockEntity be, StationRenderState state, float tickProgress) {
        if (be.resolveFlagAngle()) {
            state.layer = RenderType.cutoutMipped();
            state.flag = CachedBuffers.partial(flag, be.getBlockState());
            float value = be.flag.getValue(tickProgress);
            float progress = (float) (Math.pow(Math.min(value * 5, 1), 2));
            if (be.flag.getChaseTarget() > 0 && !be.flag.settled() && progress == 1) {
                float wiggleProgress = (value - .2f) / .8f;
                progress += (float) ((Math.sin(wiggleProgress * (2 * Mth.PI) * 4) / 8f) / Math.max(1, 8f * wiggleProgress));
            }
            float nudge = 1 / 512f;
            state.flagYRot = Mth.DEG_TO_RAD * be.flagYRot;
            boolean flipped = be.flagFlipped;
            state.flagOffsetZ = flipped ? 14f / 16f - nudge : 2f / 16f + nudge;
            state.flagXRot = Mth.DEG_TO_RAD * (flipped ? 1 : -1) * (progress * 90 + 270);
            state.flagYRot2 = flipped ? 0 : Mth.DEG_TO_RAD * 180;
        }
    }

    @Override
    public void submit(StationRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.incoming != null || state.outputs != null) {
            DepotRenderer.renderItemsOf(state.incoming, state.outputs, state.blockPos, cameraState.pos, queue, matrices, state.lightCoords);
        }
        if (state.layer != null) {
            queue.submitCustomGeometry(matrices, state.layer, state);
        }
        if (state.block != null) {
            state.block.render(matrices, queue);
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

    public static class StationRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public DepotItemState[] incoming;
        public List<DepotOutputItemState> outputs;
        public RenderType layer;
        public SuperByteBuffer flag;
        public float flagYRot;
        public float flagOffsetZ;
        public float flagXRot;
        public float flagYRot2;
        public TrackBlockRenderState block;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (flag != null) {
                flag.center().rotateY(flagYRot).translate(0.001953125f, 0.59375f, flagOffsetZ).uncenter();
                flag.rotateX(flagXRot).translate(0.03125f, 0, 0).rotateY(flagYRot2).translate(-0.03125f, 0, 0);
                flag.light(lightCoords).renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
