package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface TrackBlockRenderer {
    <Self extends Affine<Self>> void prepareTrackOverlay(
        Affine<Self> affine,
        BlockView world,
        BlockPos pos,
        BlockState state,
        BezierTrackPointLocation bezierPoint,
        AxisDirection direction,
        RenderedTrackOverlayType type
    );

    TrackBlockRenderState getAssemblyRenderState(StationBlockEntity be, Vec3d offset, World world, BlockPos pos, BlockState state);

    TrackBlockRenderState getRenderState(
        World world,
        Vec3d offset,
        BlockState trackState,
        BlockPos pos,
        AxisDirection direction,
        BezierTrackPointLocation bezier,
        RenderedTrackOverlayType type,
        float scale
    );
}
