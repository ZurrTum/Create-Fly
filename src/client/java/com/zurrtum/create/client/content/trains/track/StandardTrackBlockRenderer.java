package com.zurrtum.create.client.content.trains.track;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.function.IntFunction;

public class StandardTrackBlockRenderer implements TrackBlockRenderer {
    @Override
    public <Self extends Affine<Self>> void prepareTrackOverlay(
        Affine<Self> affine,
        BlockGetter world,
        BlockPos pos,
        BlockState state,
        @Nullable BezierTrackPointLocation bezierPoint,
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
                affine.translate(0, -4 / 16.0f, 0);
            } else {
                return;
            }
        }

        if (normal == null) {
            axis = state.getValue(TrackBlock.SHAPE).getAxes().getFirst();
            diff = axis.scale(direction.getStep()).normalize();
            normal = state.getValue(TrackBlock.SHAPE).getNormal();
        }

        Vec3 angles = TrackRenderer.getModelAngles(normal, diff);

        affine.center().rotateY((float) angles.y).rotateX((float) angles.x).uncenter();

        if (axis != null) {
            affine.translate(0, axis.y != 0 ? 7 / 16.0f : 0, axis.y != 0 ? direction.getStep() * 2.5f / 16.0f : 0);
        } else {
            affine.translate(0, 4 / 16.0f, 0);
            if (direction == AxisDirection.NEGATIVE) {
                affine.rotateYCentered(Radians.DEG_180);
            }
        }

        if (bezierPoint == null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackTE && trackTE.isTilted()) {
            double yOffset = 0;
            for (BezierConnection bc : trackTE.getConnections().values()) {
                yOffset += bc.starts.getFirst().y - pos.getY();
            }
            affine.rotateXCenteredDegrees((float) (-direction.getStep() * trackTE.tilt.smoothingAngle.get()))
                .translate(0, yOffset / 2, 0);
        }
    }

    @Override
    @Nullable
    public TrackBlockRenderState getRenderState(
        Level world,
        Vec3 offset,
        BlockState trackState,
        BlockPos pos,
        AxisDirection direction,
        @Nullable BezierTrackPointLocation bezier,
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
                offset = bc.getPosition(t).subtract(Vec3.atBottomCenterOf(pos)).add(offset).add(0, -4 / 16.0f, 0);
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
        state.yRadians = (float) angles.y;
        state.xRadians = (float) angles.x;
        if (axis != null) {
            state.offset2 = axis.y != 0 ? new Vec3(0, 7 / 16.0f, direction.getStep() * 2.5f / 16.0f) : Vec3.ZERO;
        } else if (direction == AxisDirection.NEGATIVE) {
            state.negative = true;
        }
        if (bezier == null && world.getBlockEntity(pos) instanceof TrackBlockEntity trackTE && trackTE.isTilted()) {
            double yOffset = 0;
            for (BezierConnection bc : trackTE.getConnections().values()) {
                yOffset += bc.starts.getFirst().y - pos.getY();
            }
            state.xRot2 = (float) (-direction.getStep() * trackTE.tilt.smoothingAngle.get());
            state.offset3 = (float) (yOffset / 2);
        }
        PartialModel partial = switch (type) {
            case DUAL_SIGNAL -> AllPartialModels.TRACK_SIGNAL_DUAL_OVERLAY;
            case OBSERVER -> AllPartialModels.TRACK_OBSERVER_OVERLAY;
            case SIGNAL -> AllPartialModels.TRACK_SIGNAL_OVERLAY;
            case STATION -> AllPartialModels.TRACK_STATION_OVERLAY;
        };
        int light = LightCoordsUtil.getLightCoords(world, pos);
        state.model = CachedBuffers.partial(partial, trackState).cardinalLighting(world).light(light)
            .extractRenderState();
        state.scale = scale;
        return state;
    }

    @Override
    @Nullable
    public TrackBlockRenderState getAssemblyRenderState(
        StationBlockEntity be,
        Vec3 offset,
        Level world,
        BlockPos pos,
        BlockState blockState
    ) {
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
        state.offset = offset;
        state.yRadians = Radians.horizontal(direction);
        SuperByteBuffer model = CachedBuffers.partial(AllPartialModels.TRACK_ASSEMBLING_OVERLAY, blockState);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(world);
        Int2ObjectMap<SuperByteBufferRenderState> valid = new Int2ObjectOpenHashMap<>();
        Int2ObjectMap<SuperByteBufferRenderState> carriage = new Int2ObjectOpenHashMap<>();
        IntFunction<SuperByteBufferRenderState> validGetter = i -> model.cardinalLighting(cardinalLighting).light(i)
            .color(0xFF96B5FF).extractRenderState();
        IntFunction<SuperByteBufferRenderState> carriageGetter = i -> model.cardinalLighting(cardinalLighting).light(i)
            .color(0xFFCAFF96).extractRenderState();
        MutableBlockPos currentPos = pos.mutable();
        @Nullable SuperByteBufferRenderState[] states = state.states = new SuperByteBufferRenderState[length];
        int index = 0;
        for (int location : locations) {
            if (location == -1 || location >= length) {
                break;
            }
            int i = index;
            index = location;
            for (; i < index; i++) {
                if (be.isValidBogeyOffset(i)) {
                    states[i] = valid.computeIfAbsent(
                        LightCoordsUtil.getLightCoords(
                            world,
                            currentPos.move(direction, 1)
                        ), validGetter
                    );
                }
            }
            states[i] = carriage.computeIfAbsent(
                LightCoordsUtil.getLightCoords(world, currentPos.move(direction, 1)),
                carriageGetter
            );
            index++;
        }
        for (; index < length; index++) {
            if (be.isValidBogeyOffset(index)) {
                states[index] = valid.computeIfAbsent(
                    LightCoordsUtil.getLightCoords(
                        world,
                        currentPos.move(direction, 1)
                    ), validGetter
                );
            }
        }
        return state;
    }

    public static class StandardTrackBlockRenderState implements TrackBlockRenderState {
        public @UnknownNullability Vec3 offset;
        public float yRadians;
        public float xRadians;
        public @Nullable Vec3 offset2;
        public float xRot2;
        public float offset3;
        public boolean negative;
        public @UnknownNullability SuperByteBufferRenderState model;
        public float scale;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
            if (yRadians != 0 || xRadians != 0) {
                pose.translate(0.5f, 0.5f, 0.5f);
                if (yRadians != 0) {
                    pose.rotate(Axis.YP, yRadians);
                }
                if (xRadians != 0) {
                    pose.rotate(Axis.XP, xRadians);
                }
                pose.translate(-0.5f, -0.5f, -0.5f);
            }
            if (offset2 != null) {
                if (offset2 != Vec3.ZERO) {
                    pose.translate((float) offset2.x, (float) offset2.y, (float) offset2.z);
                }
            } else {
                pose.translate(0, 0.25f, 0);
                if (negative) {
                    pose.rotateYCentered(Radians.DEG_180);
                }
            }
            if (xRot2 != 0) {
                pose.rotateXCentered(Mth.DEG_TO_RAD * xRot2);
                pose.translate(0, offset3, 0);
            }
            pose.pose().scaleAround(scale, 0.5f, 0, 0.5f);
            model.submit(matrices, queue);
            matrices.popPose();
        }
    }

    public static class StandardTrackAssemblyRenderState implements TrackBlockRenderState {
        public @UnknownNullability Vec3 offset;
        public float yRadians;
        public @Nullable SuperByteBufferRenderState @UnknownNullability [] states;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
            if (yRadians != 0) {
                pose.rotateYCentered(yRadians);
            }
            for (SuperByteBufferRenderState state : states) {
                pose.translate(0, 0, 1);
                if (state != null) {
                    state.submit(matrices, queue);
                }
            }
            matrices.popPose();
        }
    }
}
