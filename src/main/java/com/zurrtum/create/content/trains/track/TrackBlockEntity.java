package com.zurrtum.create.content.trains.track;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.blockEntity.IMergeableBE;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.packet.s2c.RemoveBlockEntityPacket;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class TrackBlockEntity extends SmartBlockEntity implements TransformableBlockEntity, IMergeableBE {

    Map<BlockPos, BezierConnection> connections;
    boolean cancelDrops;

    public Pair<ResourceKey<Level>, BlockPos> boundLocation;
    public TrackBlockEntityTilt tilt;

    public TrackBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TRACK, pos, state);
        connections = new HashMap<>();
        setLazyTickRate(100);
        tilt = new TrackBlockEntityTilt(this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        cancelDrops |= level.getBlockState(pos).is(AllBlocks.TRACK);
        removeInboundConnections(true);
    }

    public Map<BlockPos, BezierConnection> getConnections() {
        return connections;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level.isClientSide() && hasInteractableConnections())
            AllClientHandle.INSTANCE.registerToCurveInteraction(this);
    }

    @Override
    public void tick() {
        super.tick();
        tilt.undoSmoothing();
    }

    @Override
    public void lazyTick() {
        for (BezierConnection connection : connections.values())
            if (connection.isPrimary())
                manageFakeTracksAlong(connection, false);
    }

    public void validateConnections() {
        Set<BlockPos> invalid = new HashSet<>();

        for (Map.Entry<BlockPos, BezierConnection> entry : connections.entrySet()) {
            BlockPos key = entry.getKey();
            BezierConnection bc = entry.getValue();

            if (!key.equals(bc.getKey()) || !worldPosition.equals(bc.bePositions.getFirst())) {
                invalid.add(key);
                continue;
            }

            BlockState blockState = level.getBlockState(key);
            if (blockState.getBlock() instanceof ITrackBlock trackBlock && !blockState.getValue(TrackBlock.HAS_BE))
                for (Vec3 v : trackBlock.getTrackAxes(level, key, blockState)) {
                    Vec3 bcEndAxis = bc.axes.getSecond();
                    if (v.distanceTo(bcEndAxis) < 1 / 1024f || v.distanceTo(bcEndAxis.scale(-1)) < 1 / 1024f)
                        level.setBlock(key, blockState.setValue(TrackBlock.HAS_BE, true), Block.UPDATE_ALL);
                }

            BlockEntity blockEntity = level.getBlockEntity(key);
            if (!(blockEntity instanceof TrackBlockEntity trackBE) || blockEntity.isRemoved()) {
                invalid.add(key);
                continue;
            }

            if (!trackBE.connections.containsKey(worldPosition)) {
                trackBE.addConnection(bc.secondary());
                trackBE.tilt.tryApplySmoothing();
            }
        }

        for (BlockPos blockPos : invalid)
            removeConnection(blockPos);
    }

    public void addConnection(BezierConnection connection) {
        // don't replace existing connections with different materials
        if (connections.containsKey(connection.getKey()) && connection.equalsSansMaterial(connections.get(connection.getKey())))
            return;
        connections.put(connection.getKey(), connection);
        level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        notifyUpdate();

        if (connection.isPrimary())
            manageFakeTracksAlong(connection, false);
    }

    public void removeConnection(BlockPos target) {
        if (isTilted())
            tilt.captureSmoothingHandles();

        BezierConnection removed = connections.remove(target);
        notifyUpdate();

        if (removed != null)
            manageFakeTracksAlong(removed, true);

        if (!connections.isEmpty() || getBlockState().getValueOrElse(TrackBlock.SHAPE, TrackShape.NONE).isPortal())
            return;

        BlockState blockState = level.getBlockState(worldPosition);
        if (blockState.hasProperty(TrackBlock.HAS_BE))
            level.setBlockAndUpdate(worldPosition, blockState.setValue(TrackBlock.HAS_BE, false));
        if (level instanceof ServerLevel serverLevel) {
            Packet<?> packet = new RemoveBlockEntityPacket(worldPosition);
            for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(worldPosition), false)) {
                player.connection.send(packet);
            }
        }
    }

    public void removeInboundConnections(boolean dropAndDiscard) {
        for (BezierConnection bezierConnection : connections.values()) {
            if (!(level.getBlockEntity(bezierConnection.getKey()) instanceof TrackBlockEntity tbe))
                return;
            tbe.removeConnection(bezierConnection.bePositions.getFirst());
            if (!dropAndDiscard)
                continue;
            if (!cancelDrops)
                bezierConnection.spawnItems(level);
            bezierConnection.spawnDestroyParticles(level);
        }
        if (dropAndDiscard && level instanceof ServerLevel serverLevel) {
            Packet<?> packet = new RemoveBlockEntityPacket(worldPosition);
            for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(worldPosition), false)) {
                player.connection.send(packet);
            }
        }
    }

    public void bind(ResourceKey<Level> boundDimension, BlockPos boundLocation) {
        this.boundLocation = Pair.of(boundDimension, boundLocation);
        setChanged();
    }

    public boolean isTilted() {
        return tilt.smoothingAngle.isPresent();
    }

    @Override
    public void writeSafe(ValueOutput view) {
        super.writeSafe(view);
        writeTurns(view, true);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        writeTurns(view, false);
        tilt.smoothingAngle.ifPresent(angle -> view.store("Smoothing", Codec.DOUBLE, angle));
        if (boundLocation == null)
            return;
        view.store("BoundLocation", BlockPos.CODEC, boundLocation.getSecond());
        view.store("BoundDimension", Level.RESOURCE_KEY_CODEC, boundLocation.getFirst());
    }

    private void writeTurns(ValueOutput view, boolean restored) {
        ValueOutput.ValueOutputList list = view.childrenList("Connections");
        for (BezierConnection bezierConnection : connections.values())
            (restored ? tilt.restoreToOriginalCurve(bezierConnection.clone()) : bezierConnection).write(list.addChild(), worldPosition);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        connections.clear();
        view.childrenListOrEmpty("Connections").forEach(item -> {
            BezierConnection connection = new BezierConnection(item, worldPosition);
            connections.put(connection.getKey(), connection);
        });

        boolean smoothingPreviously = tilt.smoothingAngle.isPresent();
        tilt.smoothingAngle = view.read("Smoothing", Codec.DOUBLE);
        if (smoothingPreviously != tilt.smoothingAngle.isPresent() && clientPacket) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 16);
        }

        if (level != null && level.isClientSide()) {
            AllClientHandle.INSTANCE.queueUpdate(this);

            if (hasInteractableConnections())
                AllClientHandle.INSTANCE.registerToCurveInteraction(this);
            else
                AllClientHandle.INSTANCE.removeFromCurveInteraction(this);
        }

        view.read("BoundLocation", BlockPos.CODEC)
            .ifPresent(pos -> boundLocation = Pair.of(view.read("BoundDimension", Level.RESOURCE_KEY_CODEC).orElseThrow(), pos));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    public void accept(BlockEntity other) {
        if (other instanceof TrackBlockEntity track)
            connections.putAll(track.connections);
        validateConnections();
        level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
    }

    public boolean hasInteractableConnections() {
        for (BezierConnection connection : connections.values())
            if (connection.isPrimary())
                return true;
        return false;
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        Map<BlockPos, BezierConnection> restoredConnections = new HashMap<>();
        for (Map.Entry<BlockPos, BezierConnection> entry : connections.entrySet())
            restoredConnections.put(
                entry.getKey(),
                tilt.restoreToOriginalCurve(tilt.restoreToOriginalCurve(entry.getValue().secondary()).secondary())
            );
        connections = restoredConnections;
        tilt.smoothingAngle = Optional.empty();

        if (transform.rotationAxis != Axis.Y)
            return;

        Map<BlockPos, BezierConnection> transformedConnections = new HashMap<>();
        for (Map.Entry<BlockPos, BezierConnection> entry : connections.entrySet()) {
            BezierConnection newConnection = entry.getValue();
            newConnection.normals.replace(transform::applyWithoutOffsetUncentered);
            newConnection.axes.replace(transform::applyWithoutOffsetUncentered);

            BlockPos diff = newConnection.bePositions.getSecond().subtract(newConnection.bePositions.getFirst());
            newConnection.bePositions.setSecond(BlockPos.containing(Vec3.atCenterOf(newConnection.bePositions.getFirst())
                .add(transform.applyWithoutOffsetUncentered(Vec3.atLowerCornerOf(diff)))));

            Vec3 beVec = Vec3.atLowerCornerOf(worldPosition);
            Vec3 teCenterVec = beVec.add(0.5, 0.5, 0.5);
            Vec3 start = newConnection.starts.getFirst();
            Vec3 startToBE = start.subtract(teCenterVec);
            Vec3 endToStart = newConnection.starts.getSecond().subtract(start);
            startToBE = transform.applyWithoutOffsetUncentered(startToBE).add(teCenterVec);
            endToStart = transform.applyWithoutOffsetUncentered(endToStart).add(startToBE);

            newConnection.starts.setFirst(new TrackNodeLocation(startToBE).getLocation());
            newConnection.starts.setSecond(new TrackNodeLocation(endToStart).getLocation());

            BlockPos newTarget = newConnection.getKey();
            transformedConnections.put(newTarget, newConnection);
        }

        connections = transformedConnections;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (level.isClientSide())
            AllClientHandle.INSTANCE.removeFromCurveInteraction(this);
    }

    @Override
    public void remove() {
        super.remove();

        for (BezierConnection connection : connections.values())
            manageFakeTracksAlong(connection, true);

        if (boundLocation != null && level instanceof ServerLevel) {
            ServerLevel otherLevel = level.getServer().getLevel(boundLocation.getFirst());
            if (otherLevel == null)
                return;
            if (otherLevel.getBlockState(boundLocation.getSecond()).is(AllBlockTags.TRACKS))
                otherLevel.destroyBlock(boundLocation.getSecond(), false);
        }
    }

    public void manageFakeTracksAlong(BezierConnection bc, boolean remove) {
        Map<Pair<Integer, Integer>, Double> yLevels = bc.rasterise();

        for (Map.Entry<Pair<Integer, Integer>, Double> entry : yLevels.entrySet()) {
            double yValue = entry.getValue();
            int floor = Mth.floor(yValue);
            BlockPos targetPos = new BlockPos(entry.getKey().getFirst(), floor, entry.getKey().getSecond());
            targetPos = targetPos.offset(bc.bePositions.getFirst()).above(1);

            BlockState stateAtPos = level.getBlockState(targetPos);
            boolean present = stateAtPos.is(AllBlocks.FAKE_TRACK);

            if (remove) {
                if (present)
                    level.removeBlock(targetPos, false);
                continue;
            }

            FluidState fluidState = stateAtPos.getFluidState();
            if (!fluidState.isEmpty() && !fluidState.isSourceOfType(Fluids.WATER))
                continue;

            if (!present && stateAtPos.canBeReplaced())
                level.setBlock(
                    targetPos,
                    ProperWaterloggedBlock.withWater(level, AllBlocks.FAKE_TRACK.defaultBlockState(), targetPos),
                    Block.UPDATE_ALL
                );
            FakeTrackBlock.keepAlive(level, targetPos);
        }
    }

}
