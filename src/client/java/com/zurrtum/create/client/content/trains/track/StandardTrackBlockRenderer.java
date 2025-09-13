package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

public class StandardTrackBlockRenderer implements TrackBlockRenderer {

    @Override
    public PartialModel prepareAssemblyOverlay(BlockView world, BlockPos pos, BlockState state, Direction direction, MatrixStack ms) {
        TransformStack.of(ms).rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(direction)), Direction.UP);
        return AllPartialModels.TRACK_ASSEMBLING_OVERLAY;
    }

    @Override
    public <Self extends Affine<Self>> PartialModel prepareTrackOverlay(
        Affine<Self> affine,
        BlockView world,
        BlockPos pos,
        BlockState state,
        BezierTrackPointLocation bezierPoint,
        AxisDirection direction,
        TrackTargetingBehaviour.RenderedTrackOverlayType type
    ) {
        Vec3d axis = null;
        Vec3d diff = null;
        Vec3d normal = null;
        Vec3d offset = null;

        if (bezierPoint != null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackBE) {
            BezierConnection bc = trackBE.getConnections().get(bezierPoint.curveTarget());
            if (bc != null) {
                double length = MathHelper.floor(bc.getLength() * 2);
                int seg = bezierPoint.segment() + 1;
                double t = seg / length;
                double tpre = (seg - 1) / length;
                double tpost = (seg + 1) / length;

                offset = bc.getPosition(t);
                normal = bc.getNormal(t);
                diff = bc.getPosition(tpost).subtract(bc.getPosition(tpre)).normalize();

                affine.translate(offset.subtract(Vec3d.ofBottomCenter(pos)));
                affine.translate(0, -4 / 16f, 0);
            } else
                return null;
        }

        if (normal == null) {
            axis = state.get(TrackBlock.SHAPE).getAxes().get(0);
            diff = axis.multiply(direction.offset()).normalize();
            normal = state.get(TrackBlock.SHAPE).getNormal();
        }

        Vec3d angles = TrackRenderer.getModelAngles(normal, diff);

        affine.center().rotateY((float) angles.y).rotateX((float) angles.x).uncenter();

        if (axis != null)
            affine.translate(0, axis.y != 0 ? 7 / 16f : 0, axis.y != 0 ? direction.offset() * 2.5f / 16f : 0);
        else {
            affine.translate(0, 4 / 16f, 0);
            if (direction == AxisDirection.NEGATIVE)
                affine.rotateCentered(MathHelper.PI, Direction.UP);
        }

        if (bezierPoint == null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackTE && trackTE.isTilted()) {
            double yOffset = 0;
            for (BezierConnection bc : trackTE.getConnections().values())
                yOffset += bc.starts.getFirst().y - pos.getY();
            affine.center().rotateXDegrees((float) (-direction.offset() * trackTE.tilt.smoothingAngle.get())).uncenter().translate(0, yOffset / 2, 0);
        }

        return switch (type) {
            case DUAL_SIGNAL -> AllPartialModels.TRACK_SIGNAL_DUAL_OVERLAY;
            case OBSERVER -> AllPartialModels.TRACK_OBSERVER_OVERLAY;
            case SIGNAL -> AllPartialModels.TRACK_SIGNAL_OVERLAY;
            case STATION -> AllPartialModels.TRACK_STATION_OVERLAY;
        };
    }
}
