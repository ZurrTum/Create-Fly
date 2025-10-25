package com.zurrtum.create.client.content.trains.station;

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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.ItemModelManager;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StationRenderer implements BlockEntityRenderer<StationBlockEntity, StationRenderer.StationRenderState> {
    protected final ItemModelManager itemModelManager;

    public StationRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    public StationRenderState createRenderState() {
        return new StationRenderState();
    }

    @Override
    public void updateRenderState(
        StationBlockEntity be,
        StationRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        state.pos = be.getPos();
        state.type = be.getType();
        World world = be.getWorld();
        state.lightmapCoordinates = world != null ? WorldRenderer.getLightmapCoordinates(
            world,
            state.pos
        ) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
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
        boolean isAssembling = be.getCachedState().get(StationBlock.ASSEMBLING);
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
                    world,
                    new Vec3d(
                        targetPosition.getX() - state.pos.getX(),
                        targetPosition.getY() - state.pos.getY(),
                        targetPosition.getZ() - state.pos.getZ()
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
        updateFlagState(AllPartialModels.STATION_ASSEMBLE, be, state, tickProgress);
        if (be.isVirtual() && be.bogeyLocations == null) {
            be.refreshAssemblyInfo();
        }
        TrackBlockRenderer renderer = AllTrackRenders.get(track);
        if (renderer == null) {
            return;
        }
        state.block = renderer.getAssemblyRenderState(
            be,
            new Vec3d(targetPosition.getX() - state.pos.getX(), targetPosition.getY() - state.pos.getY(), targetPosition.getZ() - state.pos.getZ()),
            world,
            targetPosition,
            trackState
        );
    }

    public void updateFlagState(PartialModel flag, StationBlockEntity be, StationRenderState state, float tickProgress) {
        if (be.resolveFlagAngle()) {
            state.layer = RenderLayer.getCutoutMipped();
            state.flag = CachedBuffers.partial(flag, be.getCachedState());
            float value = be.flag.getValue(tickProgress);
            float progress = (float) (Math.pow(Math.min(value * 5, 1), 2));
            if (be.flag.getChaseTarget() > 0 && !be.flag.settled() && progress == 1) {
                float wiggleProgress = (value - .2f) / .8f;
                progress += (float) ((Math.sin(wiggleProgress * (2 * MathHelper.PI) * 4) / 8f) / Math.max(1, 8f * wiggleProgress));
            }
            float nudge = 1 / 512f;
            state.flagYRot = MathHelper.RADIANS_PER_DEGREE * be.flagYRot;
            boolean flipped = be.flagFlipped;
            state.flagOffsetZ = flipped ? 14f / 16f - nudge : 2f / 16f + nudge;
            state.flagXRot = MathHelper.RADIANS_PER_DEGREE * (flipped ? 1 : -1) * (progress * 90 + 270);
            state.flagYRot2 = flipped ? 0 : MathHelper.RADIANS_PER_DEGREE * 180;
        }
    }

    @Override
    public void render(StationRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.incoming != null || state.outputs != null) {
            DepotRenderer.renderItemsOf(state.incoming, state.outputs, state.pos, cameraState.pos, queue, matrices, state.lightmapCoordinates);
        }
        if (state.layer != null) {
            queue.submitCustom(matrices, state.layer, state);
        }
        if (state.block != null) {
            state.block.render(matrices, queue);
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 96 * 2;
    }

    public static class StationRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public DepotItemState[] incoming;
        public List<DepotOutputItemState> outputs;
        public RenderLayer layer;
        public SuperByteBuffer flag;
        public float flagYRot;
        public float flagOffsetZ;
        public float flagXRot;
        public float flagYRot2;
        public TrackBlockRenderState block;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            if (flag != null) {
                flag.center().rotateY(flagYRot).translate(0.001953125f, 0.59375f, flagOffsetZ).uncenter();
                flag.rotateX(flagXRot).translate(0.03125f, 0, 0).rotateY(flagYRot2).translate(-0.03125f, 0, 0);
                flag.light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
