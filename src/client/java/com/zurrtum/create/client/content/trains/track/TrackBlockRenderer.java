package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public interface TrackBlockRenderer {
    <Self extends Affine<Self>> PartialModel prepareTrackOverlay(
        Affine<Self> affine,
        BlockView world,
        BlockPos pos,
        BlockState state,
        BezierTrackPointLocation bezierPoint,
        AxisDirection direction,
        RenderedTrackOverlayType type
    );

    PartialModel prepareAssemblyOverlay(BlockView world, BlockPos pos, BlockState state, Direction direction, MatrixStack ms);

    default void render(
        WorldAccess level,
        BlockState trackState,
        BlockPos pos,
        AxisDirection direction,
        BezierTrackPointLocation bezier,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay,
        RenderedTrackOverlayType type,
        float scale
    ) {
        if (level instanceof SchematicLevel && !(level instanceof PonderLevel))
            return;

        ms.push();
        var msr = TransformStack.of(ms);
        PartialModel partial = prepareTrackOverlay(msr, level, pos, trackState, bezier, direction, type);
        if (partial != null)
            CachedBuffers.partial(partial, trackState).translate(.5, 0, .5).scale(scale).translate(-.5, 0, -.5)
                .light(WorldRenderer.getLightmapCoordinates(level, pos)).renderInto(ms, buffer.getBuffer(RenderLayer.getCutoutMipped()));
        ms.pop();
    }
}
