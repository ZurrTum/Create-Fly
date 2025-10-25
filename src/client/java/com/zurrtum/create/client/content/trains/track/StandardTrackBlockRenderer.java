package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.joml.Quaternionf;

public class StandardTrackBlockRenderer implements TrackBlockRenderer {
    @Override
    public PartialModel prepareAssemblyOverlay(BlockView world, BlockPos pos, BlockState state, Direction direction, MatrixStack ms) {
        TransformStack.of(ms).rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(direction)), Direction.UP);
        return AllPartialModels.TRACK_ASSEMBLING_OVERLAY;
    }

    @Override
    public <Self extends Affine<Self>> void prepareTrackOverlay(
        Affine<Self> affine,
        BlockView world,
        BlockPos pos,
        BlockState state,
        BezierTrackPointLocation bezierPoint,
        AxisDirection direction,
        RenderedTrackOverlayType type
    ) {
        Vec3d axis = null;
        Vec3d diff = null;
        Vec3d normal = null;
        if (bezierPoint != null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackBE) {
            BezierConnection bc = trackBE.getConnections().get(bezierPoint.curveTarget());
            if (bc != null) {
                double length = MathHelper.floor(bc.getLength() * 2);
                int seg = bezierPoint.segment() + 1;
                double t = seg / length;
                double tpre = (seg - 1) / length;
                double tpost = (seg + 1) / length;

                Vec3d offset = bc.getPosition(t);
                normal = bc.getNormal(t);
                diff = bc.getPosition(tpost).subtract(bc.getPosition(tpre)).normalize();

                affine.translate(offset.subtract(Vec3d.ofBottomCenter(pos)));
                affine.translate(0, -4 / 16f, 0);
            } else
                return;
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
    }

    @Override
    public TrackBlockRenderState getRenderState(
        World world,
        BlockState trackState,
        BlockPos pos,
        AxisDirection direction,
        BezierTrackPointLocation bezier,
        RenderedTrackOverlayType type,
        float scale
    ) {
        if (world instanceof SchematicLevel && !(world instanceof PonderLevel)) {
            return null;
        }
        Vec3d axis = null;
        Vec3d diff = null;
        Vec3d normal = null;
        Vec3d offset = null;
        if (bezier != null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackBE) {
            BezierConnection bc = trackBE.getConnections().get(bezier.curveTarget());
            if (bc != null) {
                double length = MathHelper.floor(bc.getLength() * 2);
                int seg = bezier.segment() + 1;
                double t = seg / length;
                double tpre = (seg - 1) / length;
                double tpost = (seg + 1) / length;
                offset = bc.getPosition(t).subtract(Vec3d.ofBottomCenter(pos)).add(0, -4 / 16f, 0);
                normal = bc.getNormal(t);
                diff = bc.getPosition(tpost).subtract(bc.getPosition(tpre)).normalize();
            } else {
                return null;
            }
        }
        if (normal == null) {
            axis = trackState.get(TrackBlock.SHAPE).getAxes().getFirst();
            diff = axis.multiply(direction.offset()).normalize();
            normal = trackState.get(TrackBlock.SHAPE).getNormal();
        }
        StandardTrackBlockRenderState state = new StandardTrackBlockRenderState();
        state.offset = offset;
        Vec3d angles = TrackRenderer.getModelAngles(normal, diff);
        state.yRot = (float) angles.y;
        state.xRot = (float) angles.x;
        if (axis != null) {
            state.offset2 = axis.y != 0 ? new Vec3d(0, 7 / 16f, direction.offset() * 2.5f / 16f) : Vec3d.ZERO;
        } else if (direction == AxisDirection.NEGATIVE) {
            state.negative = true;
        }
        if (bezier == null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackTE && trackTE.isTilted()) {
            double yOffset = 0;
            for (BezierConnection bc : trackTE.getConnections().values()) {
                yOffset += bc.starts.getFirst().y - pos.getY();
            }
            state.xRot2 = MathHelper.RADIANS_PER_DEGREE * (float) (-direction.offset() * trackTE.tilt.smoothingAngle.get());
            state.offset3 = (float) (yOffset / 2);
        }
        state.layer = RenderLayer.getCutoutMipped();
        PartialModel partial = switch (type) {
            case DUAL_SIGNAL -> AllPartialModels.TRACK_SIGNAL_DUAL_OVERLAY;
            case OBSERVER -> AllPartialModels.TRACK_OBSERVER_OVERLAY;
            case SIGNAL -> AllPartialModels.TRACK_SIGNAL_OVERLAY;
            case STATION -> AllPartialModels.TRACK_STATION_OVERLAY;
        };
        state.model = CachedBuffers.partial(partial, trackState);
        state.scale = scale;
        state.light = WorldRenderer.getLightmapCoordinates(world, pos);
        return state;
    }

    public static class StandardTrackBlockRenderState extends TrackBlockRenderState {
        public Vec3d offset;
        public float yRot;
        public float xRot;
        public Vec3d offset2;
        public float xRot2;
        public Float offset3;
        public boolean negative;
        public SuperByteBuffer model;
        public int light;
        public float scale;

        @Override
        public void transform(MatrixStack matrices) {
            if (offset != null) {
                matrices.translate(offset);
            }
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(xRot));
            matrices.translate(-0.5f, -0.5f, -0.5f);
            if (offset2 != null) {
                if (offset2 != Vec3d.ZERO) {
                    matrices.translate(offset2);
                }
            } else {
                matrices.translate(0, 0.25f, 0);
                if (negative) {
                    matrices.multiply(new Quaternionf().setAngleAxis(Math.PI, 0, 1, 0), 0.5f, 0.5f, 0.5f);
                }
            }
            if (offset3 != null) {
                matrices.translate(0.5f, 0.5f, 0.5f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(xRot2));
                matrices.translate(-0.5f, -0.5f, -0.5f);
                matrices.translate(0, offset3, 0);
            }
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.translate(.5f, 0, .5f).scale(scale).translate(-.5f, 0, -.5f).light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
