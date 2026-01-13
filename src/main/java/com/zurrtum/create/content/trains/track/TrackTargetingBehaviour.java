package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.trains.graph.*;
import com.zurrtum.create.content.trains.signal.SingleBlockEntityEdgePoint;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TrackTargetingBehaviour<T extends TrackEdgePoint> extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<TrackTargetingBehaviour<?>> TYPE = new BehaviourType<>();

    private BlockPos targetTrack;
    private BezierTrackPointLocation targetBezier;
    private AxisDirection targetDirection;
    private UUID id;

    private Vec3d prevDirection;
    private Vec3d rotatedDirection;

    private NbtCompound migrationData;
    private EdgePointType<T> edgePointType;
    private T edgePoint;
    private boolean orthogonal;

    public TrackTargetingBehaviour(SmartBlockEntity be, EdgePointType<T> edgePointType) {
        super(be);
        this.edgePointType = edgePointType;
        targetDirection = AxisDirection.POSITIVE;
        targetTrack = BlockPos.ORIGIN;
        id = UUID.randomUUID();
        migrationData = null;
        orthogonal = false;
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.put("Id", Uuids.INT_STREAM_CODEC, id);
        view.put("TargetTrack", BlockPos.CODEC, targetTrack);
        view.putBoolean("Ortho", orthogonal);
        view.putBoolean("TargetDirection", targetDirection == AxisDirection.POSITIVE);
        if (rotatedDirection != null)
            view.put("RotatedAxis", Vec3d.CODEC, rotatedDirection);
        if (prevDirection != null)
            view.put("PrevAxis", Vec3d.CODEC, prevDirection);
        if (migrationData != null && !clientPacket)
            view.put("Migrate", NbtCompound.CODEC, migrationData);
        if (targetBezier != null) {
            WriteView bezier = view.get("Bezier");
            bezier.putInt("Segment", targetBezier.segment());
            bezier.put("Key", BlockPos.CODEC, targetBezier.curveTarget().subtract(getPos()));
        }
        super.write(view, clientPacket);
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        id = view.read("Id", Uuids.INT_STREAM_CODEC).orElseGet(UUID::randomUUID);
        targetTrack = view.read("TargetTrack", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        targetDirection = view.getBoolean("TargetDirection", false) ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        orthogonal = view.getBoolean("Ortho", false);
        view.read("RotatedAxis", Vec3d.CODEC).ifPresent(rotated -> rotatedDirection = rotated);
        view.read("PrevAxis", Vec3d.CODEC).ifPresent(prev -> prevDirection = prev);
        view.read("Migrate", NbtCompound.CODEC).ifPresent(migration -> migrationData = migration);
        if (clientPacket)
            edgePoint = null;
        view.getOptionalReadView("Bezier").ifPresent(bezier -> {
            BlockPos key = bezier.read("Key", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
            targetBezier = new BezierTrackPointLocation(key.add(getPos()), bezier.getInt("Segment", 0));
        });
        super.read(view, clientPacket);
    }

    @Nullable
    public T getEdgePoint() {
        return edgePoint;
    }

    public void invalidateEdgePoint(NbtCompound migrationData) {
        this.migrationData = migrationData;
        edgePoint = null;
        blockEntity.sendData();
    }

    @Override
    public void tick() {
        super.tick();
        if (edgePoint == null)
            edgePoint = createEdgePoint();
    }

    @SuppressWarnings("unchecked")
    public T createEdgePoint() {
        World level = getWorld();
        boolean isClientSide = level.isClient;
        if (migrationData == null || isClientSide)
            for (TrackGraph trackGraph : Create.RAILWAYS.sided(level).trackNetworks.values()) {
                T point = trackGraph.getPoint(edgePointType, id);
                if (point == null)
                    continue;
                return point;
            }

        if (isClientSide)
            return null;
        if (!hasValidTrack())
            return null;
        TrackGraphLocation loc = determineGraphLocation();
        if (loc == null)
            return null;

        TrackGraph graph = loc.graph;
        TrackNode node1 = graph.locateNode(loc.edge.getFirst());
        TrackNode node2 = graph.locateNode(loc.edge.getSecond());
        TrackEdge edge = graph.getConnectionsFrom(node1).get(node2);
        if (edge == null)
            return null;

        T point = edgePointType.create();
        boolean front = getTargetDirection() == AxisDirection.POSITIVE;

        prevDirection = edge.getDirectionAt(loc.position).multiply(front ? -1 : 1);

        if (rotatedDirection != null) {
            double dot = prevDirection.dotProduct(rotatedDirection);
            if (dot < -.85f) {
                rotatedDirection = null;
                targetDirection = targetDirection.getOpposite();
                return null;
            }

            rotatedDirection = null;
        }

        double length = edge.getLength();
        NbtCompound data = migrationData;
        migrationData = null;

        {
            orthogonal = targetBezier == null;
            Vec3d direction = edge.getDirection(true);
            int nonZeroComponents = 0;
            for (Axis axis : Iterate.axes)
                nonZeroComponents += direction.getComponentAlongAxis(axis) != 0 ? 1 : 0;
            orthogonal &= nonZeroComponents <= 1;
        }

        EdgeData signalData = edge.getEdgeData();
        if (signalData.hasPoints()) {
            for (EdgePointType<?> otherType : EdgePointType.TYPES.values()) {
                TrackEdgePoint otherPoint = signalData.get(otherType, loc.position);
                if (otherPoint == null)
                    continue;
                if (otherType != edgePointType) {
                    if (!otherPoint.canCoexistWith(edgePointType, front))
                        return null;
                    continue;
                }
                if (!otherPoint.canMerge())
                    return null;
                otherPoint.blockEntityAdded(blockEntity, front);
                id = otherPoint.getId();
                blockEntity.notifyUpdate();
                return (T) otherPoint;
            }
        }

        if (data != null) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(blockEntity.getReporterContext(), Create.LOGGER)) {
                ReadView view = NbtReadView.create(logging, level.getRegistryManager(), data);
                DimensionPalette dimensions = view.read("DimensionPalette", DimensionPalette.CODEC).orElseThrow();
                point.read(view, true, dimensions);
            }
        }

        point.setId(id);
        boolean reverseEdge = front || point instanceof SingleBlockEntityEdgePoint;
        point.setLocation(reverseEdge ? loc.edge : loc.edge.swap(), reverseEdge ? loc.position : length - loc.position);
        point.blockEntityAdded(blockEntity, front);
        loc.graph.addPoint(level.getServer(), edgePointType, point);
        blockEntity.sendData();
        return point;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (edgePoint != null) {
            World world = getWorld();
            if (!world.isClient) {
                edgePoint.blockEntityRemoved(world.getServer(), getPos(), getTargetDirection() == AxisDirection.POSITIVE);
            }
        }
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public boolean isOnCurve() {
        return targetBezier != null;
    }

    public boolean isOrthogonal() {
        return orthogonal;
    }

    public boolean hasValidTrack() {
        return getTrackBlockState().getBlock() instanceof ITrackBlock;
    }

    public ITrackBlock getTrack() {
        return (ITrackBlock) getTrackBlockState().getBlock();
    }

    public BlockState getTrackBlockState() {
        return getWorld().getBlockState(getGlobalPosition());
    }

    public BlockPos getGlobalPosition() {
        return targetTrack.add(blockEntity.getPos());
    }

    public BlockPos getPositionForMapMarker() {
        BlockPos target = targetTrack.add(blockEntity.getPos());
        if (targetBezier != null && getWorld().getBlockEntity(target) instanceof TrackBlockEntity tbe) {
            BezierConnection bc = tbe.getConnections().get(targetBezier.curveTarget());
            if (bc == null)
                return target;
            double length = MathHelper.floor(bc.getLength() * 2);
            int seg = targetBezier.segment() + 1;
            double t = seg / length;
            return BlockPos.ofFloored(bc.getPosition(t));
        }
        return target;
    }

    public AxisDirection getTargetDirection() {
        return targetDirection;
    }

    public BezierTrackPointLocation getTargetBezier() {
        return targetBezier;
    }

    public TrackGraphLocation determineGraphLocation() {
        World level = getWorld();
        BlockPos pos = getGlobalPosition();
        BlockState state = getTrackBlockState();
        ITrackBlock track = getTrack();
        List<Vec3d> trackAxes = track.getTrackAxes(level, pos, state);
        AxisDirection targetDirection = getTargetDirection();

        return targetBezier != null ? TrackGraphHelper.getBezierGraphLocationAt(
            level,
            pos,
            targetDirection,
            targetBezier
        ) : TrackGraphHelper.getGraphLocationAt(level, pos, targetDirection, trackAxes.getFirst());
    }

    public enum RenderedTrackOverlayType {
        STATION,
        SIGNAL,
        DUAL_SIGNAL,
        OBSERVER;
    }

    public void transform(BlockEntity be, StructureTransform transform) {
        id = UUID.randomUUID();
        targetTrack = transform.applyWithoutOffset(targetTrack);
        if (prevDirection != null)
            rotatedDirection = transform.applyWithoutOffsetUncentered(prevDirection);
        if (targetBezier != null)
            targetBezier = new BezierTrackPointLocation(
                transform.applyWithoutOffset(targetBezier.curveTarget().subtract(getPos())).add(getPos()),
                targetBezier.segment()
            );
        blockEntity.notifyUpdate();
    }

}
