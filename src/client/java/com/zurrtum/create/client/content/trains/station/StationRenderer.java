package com.zurrtum.create.client.content.trains.station;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Transform;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlock;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class StationRenderer extends SafeBlockEntityRenderer<StationBlockEntity> {

    public StationRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(StationBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {

        BlockPos pos = be.getPos();
        TrackTargetingBehaviour<GlobalStation> target = be.edgePoint;
        BlockPos targetPosition = target.getGlobalPosition();
        World level = be.getWorld();

        DepotRenderer.renderItemsOf(be, partialTicks, ms, buffer, light, overlay, be.depotBehaviour);

        BlockState trackState = level.getBlockState(targetPosition);
        Block block = trackState.getBlock();
        if (!(block instanceof ITrackBlock track))
            return;

        GlobalStation station = be.getStation();
        boolean isAssembling = be.getCachedState().get(StationBlock.ASSEMBLING);

        if (!isAssembling || (station == null || station.getPresentTrain() != null) && !be.isVirtual()) {
            renderFlag(
                be.flag.getValue(partialTicks) > 0.75f ? AllPartialModels.STATION_ON : AllPartialModels.STATION_OFF,
                be,
                partialTicks,
                ms,
                buffer,
                light,
                overlay
            );
            TrackBlockRenderer renderer = AllTrackRenders.get(track);
            if (renderer != null) {
                ms.push();
                TransformStack.of(ms).translate(targetPosition.subtract(pos));
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
                    RenderedTrackOverlayType.STATION,
                    1
                );
                ms.pop();
            }
            return;
        }

        renderFlag(AllPartialModels.STATION_ASSEMBLE, be, partialTicks, ms, buffer, light, overlay);

        Direction direction = be.assemblyDirection;

        if (be.isVirtual() && be.bogeyLocations == null)
            be.refreshAssemblyInfo();

        if (direction == null || be.assemblyLength == 0 || be.bogeyLocations == null)
            return;

        ms.push();
        BlockPos offset = targetPosition.subtract(pos);
        ms.translate(offset.getX(), offset.getY(), offset.getZ());

        BlockPos.Mutable currentPos = targetPosition.mutableCopy();

        PartialModel assemblyOverlay = null;
        TrackBlockRenderer renderer = AllTrackRenders.get(track);
        if (renderer != null) {
            assemblyOverlay = renderer.prepareAssemblyOverlay(level, targetPosition, trackState, direction, ms);
        }
        int colorWhenValid = 0x96B5FF;
        int colorWhenCarriage = 0xCAFF96;
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getCutoutMipped());

        currentPos.move(direction, 1);
        ms.translate(0, 0, 1);

        for (int i = 0; i < be.assemblyLength; i++) {
            int valid = be.isValidBogeyOffset(i) ? colorWhenValid : -1;

            for (int j : be.bogeyLocations)
                if (i == j) {
                    valid = colorWhenCarriage;
                    break;
                }

            if (valid != -1 && assemblyOverlay != null) {
                int lightColor = WorldRenderer.getLightmapCoordinates(level, currentPos);
                SuperByteBuffer sbb = CachedBuffers.partial(assemblyOverlay, trackState);
                sbb.color(valid);
                sbb.light(lightColor);
                sbb.renderInto(ms, vb);
            }
            ms.translate(0, 0, 1);
            currentPos.move(direction);
        }

        ms.pop();
    }

    public static void renderFlag(
        PartialModel flag,
        StationBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        if (!be.resolveFlagAngle())
            return;
        SuperByteBuffer flagBB = CachedBuffers.partial(flag, be.getCachedState());
        transformFlag(flagBB, be, partialTicks, be.flagYRot, be.flagFlipped);
        flagBB.translate(0.5f / 16, 0, 0).rotateYDegrees(be.flagFlipped ? 0 : 180).translate(-0.5f / 16, 0, 0).light(light)
            .renderInto(ms, buffer.getBuffer(RenderLayer.getCutoutMipped()));
    }

    public static void transformFlag(Transform<?> flag, StationBlockEntity be, float partialTicks, int yRot, boolean flipped) {
        float value = be.flag.getValue(partialTicks);
        float progress = (float) (Math.pow(Math.min(value * 5, 1), 2));
        if (be.flag.getChaseTarget() > 0 && !be.flag.settled() && progress == 1) {
            float wiggleProgress = (value - .2f) / .8f;
            progress += (Math.sin(wiggleProgress * (2 * MathHelper.PI) * 4) / 8f) / Math.max(1, 8f * wiggleProgress);
        }

        float nudge = 1 / 512f;
        flag.center().rotateYDegrees(yRot).translate(nudge, 9.5f / 16f, flipped ? 14f / 16f - nudge : 2f / 16f + nudge).uncenter()
            .rotateXDegrees((flipped ? 1 : -1) * (progress * 90 + 270));
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 96 * 2;
    }

}
