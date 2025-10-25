package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
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

    PartialModel prepareAssemblyOverlay(BlockView world, BlockPos pos, BlockState state, Direction direction, MatrixStack ms);

    TrackBlockRenderState getRenderState(
        World world,
        BlockState trackState,
        BlockPos pos,
        AxisDirection direction,
        BezierTrackPointLocation bezier,
        RenderedTrackOverlayType type,
        float scale
    );
}
