package com.zurrtum.create.client.content.trains.track;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class StandardTrackBlockRenderer implements TrackBlockRenderer {
    @Override
    public <Self extends Affine<Self>> void prepareTrackOverlay(
        Affine<Self> affine,
        BlockGetter world,
        BlockPos pos,
        BlockState state,
        BezierTrackPointLocation bezierPoint,
        AxisDirection direction,
        RenderedTrackOverlayType type
    ) {
        Vec3 axis = null;
        Vec3 diff = null;
        Vec3 normal = null;
        if (bezierPoint != null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackBE) {
            BezierConnection bc = trackBE.getConnections().get(bezierPoint.curveTarget());
            if (bc != null) {
                double length = Mth.floor(bc.getLength() * 2);
                int seg = bezierPoint.segment() + 1;
                double t = seg / length;
                double tpre = (seg - 1) / length;
                double tpost = (seg + 1) / length;

                Vec3 offset = bc.getPosition(t);
                normal = bc.getNormal(t);
                diff = bc.getPosition(tpost).subtract(bc.getPosition(tpre)).normalize();

                affine.translate(offset.subtract(Vec3.atBottomCenterOf(pos)));
                affine.translate(0, -4 / 16f, 0);
            } else
                return;
        }

        if (normal == null) {
            axis = state.getValue(TrackBlock.SHAPE).getAxes().get(0);
            diff = axis.scale(direction.getStep()).normalize();
            normal = state.getValue(TrackBlock.SHAPE).getNormal();
        }

        Vec3 angles = TrackRenderer.getModelAngles(normal, diff);

        affine.center().rotateY((float) angles.y).rotateX((float) angles.x).uncenter();

        if (axis != null)
            affine.translate(0, axis.y != 0 ? 7 / 16f : 0, axis.y != 0 ? direction.getStep() * 2.5f / 16f : 0);
        else {
            affine.translate(0, 4 / 16f, 0);
            if (direction == AxisDirection.NEGATIVE)
                affine.rotateCentered(Mth.PI, Direction.UP);
        }

        if (bezierPoint == null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackTE && trackTE.isTilted()) {
            double yOffset = 0;
            for (BezierConnection bc : trackTE.getConnections().values())
                yOffset += bc.starts.getFirst().y - pos.getY();
            affine.center().rotateXDegrees((float) (-direction.getStep() * trackTE.tilt.smoothingAngle.get())).uncenter()
                .translate(0, yOffset / 2, 0);
        }
    }

    @Override
    public TrackBlockRenderState getRenderState(
        Level world,
        Vec3 offset,
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
        Vec3 axis = null;
        Vec3 diff = null;
        Vec3 normal = null;
        if (bezier != null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackBE) {
            BezierConnection bc = trackBE.getConnections().get(bezier.curveTarget());
            if (bc != null) {
                double length = Mth.floor(bc.getLength() * 2);
                int seg = bezier.segment() + 1;
                double t = seg / length;
                double tpre = (seg - 1) / length;
                double tpost = (seg + 1) / length;
                offset = bc.getPosition(t).subtract(Vec3.atBottomCenterOf(pos)).add(offset).add(0, -4 / 16f, 0);
                normal = bc.getNormal(t);
                diff = bc.getPosition(tpost).subtract(bc.getPosition(tpre)).normalize();
            } else {
                return null;
            }
        }
        if (normal == null) {
            axis = trackState.getValue(TrackBlock.SHAPE).getAxes().getFirst();
            diff = axis.scale(direction.getStep()).normalize();
            normal = trackState.getValue(TrackBlock.SHAPE).getNormal();
        }
        StandardTrackBlockRenderState state = new StandardTrackBlockRenderState();
        state.offset = offset;
        Vec3 angles = TrackRenderer.getModelAngles(normal, diff);
        state.yRot = (float) angles.y;
        state.xRot = (float) angles.x;
        if (axis != null) {
            state.offset2 = axis.y != 0 ? new Vec3(0, 7 / 16f, direction.getStep() * 2.5f / 16f) : Vec3.ZERO;
        } else if (direction == AxisDirection.NEGATIVE) {
            state.negative = true;
        }
        if (bezier == null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackTE && trackTE.isTilted()) {
            double yOffset = 0;
            for (BezierConnection bc : trackTE.getConnections().values()) {
                yOffset += bc.starts.getFirst().y - pos.getY();
            }
            state.xRot2 = Mth.DEG_TO_RAD * (float) (-direction.getStep() * trackTE.tilt.smoothingAngle.get());
            state.offset3 = (float) (yOffset / 2);
        }
        state.layer = RenderType.cutoutMipped();
        PartialModel partial = switch (type) {
            case DUAL_SIGNAL -> AllPartialModels.TRACK_SIGNAL_DUAL_OVERLAY;
            case OBSERVER -> AllPartialModels.TRACK_OBSERVER_OVERLAY;
            case SIGNAL -> AllPartialModels.TRACK_SIGNAL_OVERLAY;
            case STATION -> AllPartialModels.TRACK_STATION_OVERLAY;
        };
        state.model = CachedBuffers.partial(partial, trackState);
        state.scale = scale;
        state.light = LevelRenderer.getLightColor(world, pos);
        return state;
    }

    @Override
    public TrackBlockRenderState getAssemblyRenderState(StationBlockEntity be, Vec3 offset, Level world, BlockPos pos, BlockState blockState) {
        Direction direction = be.assemblyDirection;
        if (direction == null) {
            return null;
        }
        int length = be.assemblyLength;
        if (length == 0) {
            return null;
        }
        int[] locations = be.bogeyLocations;
        if (locations == null) {
            return null;
        }
        StandardTrackAssemblyRenderState state = new StandardTrackAssemblyRenderState();
        state.layer = RenderType.cutoutMipped();
        state.offset = offset;
        state.angle = AngleHelper.rad(AngleHelper.horizontalAngle(direction));
        state.model = CachedBuffers.partial(AllPartialModels.TRACK_ASSEMBLING_OVERLAY, blockState);
        int colorWhenValid = 0x96B5FF;
        int colorWhenCarriage = 0xCAFF96;
        BlockPos.MutableBlockPos currentPos = pos.mutable();
        int[][] data = state.data = new int[length][];
        int index = 0;
        for (int location : locations) {
            if (location == -1) {
                break;
            }
            int i = index;
            index = location;
            for (; i < index; i++) {
                if (be.isValidBogeyOffset(i)) {
                    data[i] = new int[]{colorWhenValid, LevelRenderer.getLightColor(world, currentPos.move(direction, 1))};
                }
            }
            data[i] = new int[]{colorWhenCarriage, LevelRenderer.getLightColor(world, currentPos.move(direction, 1))};
            index++;
        }
        for (; index < length; index++) {
            if (be.isValidBogeyOffset(index)) {
                data[index] = new int[]{colorWhenValid, LevelRenderer.getLightColor(world, currentPos.move(direction, 1))};
            }
        }
        return state;
    }

    public static class StandardTrackBlockRenderState extends TrackBlockRenderState {
        public Vec3 offset;
        public float yRot;
        public float xRot;
        public Vec3 offset2;
        public float xRot2;
        public Float offset3;
        public boolean negative;
        public SuperByteBuffer model;
        public int light;
        public float scale;

        @Override
        public void transform(PoseStack matrices) {
            matrices.translate(offset);
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.mulPose(Axis.YP.rotation(yRot));
            matrices.mulPose(Axis.XP.rotation(xRot));
            matrices.translate(-0.5f, -0.5f, -0.5f);
            if (offset2 != null) {
                if (offset2 != Vec3.ZERO) {
                    matrices.translate(offset2);
                }
            } else {
                matrices.translate(0, 0.25f, 0);
                if (negative) {
                    matrices.rotateAround(new Quaternionf().setAngleAxis(Math.PI, 0, 1, 0), 0.5f, 0.5f, 0.5f);
                }
            }
            if (offset3 != null) {
                matrices.translate(0.5f, 0.5f, 0.5f);
                matrices.mulPose(Axis.XP.rotation(xRot2));
                matrices.translate(-0.5f, -0.5f, -0.5f);
                matrices.translate(0, offset3, 0);
            }
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.translate(.5f, 0, .5f).scale(scale).translate(-.5f, 0, -.5f).light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }

    public static class StandardTrackAssemblyRenderState extends TrackBlockRenderState {
        public Vec3 offset;
        public float angle;
        public SuperByteBuffer model;
        public int[][] data;

        @Override
        public void transform(PoseStack matrices) {
            matrices.translate(offset);
            matrices.rotateAround(new Quaternionf().setAngleAxis(angle, 0, 1, 0), 0.5f, 0.5f, 0.5f);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            for (int[] pair : data) {
                matricesEntry.translate(0, 0, 1);
                if (pair != null) {
                    model.color(pair[0]).light(pair[1]).renderInto(matricesEntry, vertexConsumer);
                }
            }
        }
    }
}
