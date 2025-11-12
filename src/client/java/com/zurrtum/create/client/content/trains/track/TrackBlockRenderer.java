package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface TrackBlockRenderer {
    <Self extends Affine<Self>> void prepareTrackOverlay(
        Affine<Self> affine,
        BlockGetter world,
        BlockPos pos,
        BlockState state,
        BezierTrackPointLocation bezierPoint,
        AxisDirection direction,
        RenderedTrackOverlayType type
    );

    TrackBlockRenderState getAssemblyRenderState(StationBlockEntity be, Vec3 offset, Level world, BlockPos pos, BlockState state);

    TrackBlockRenderState getRenderState(
        Level world,
        Vec3 offset,
        BlockState trackState,
        BlockPos pos,
        AxisDirection direction,
        BezierTrackPointLocation bezier,
        RenderedTrackOverlayType type,
        float scale
    );
}
