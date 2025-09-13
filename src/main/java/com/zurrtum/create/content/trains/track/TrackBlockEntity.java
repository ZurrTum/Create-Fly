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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class TrackBlockEntity extends SmartBlockEntity implements TransformableBlockEntity, IMergeableBE {

    Map<BlockPos, BezierConnection> connections;
    boolean cancelDrops;

    public Pair<RegistryKey<World>, BlockPos> boundLocation;
    public TrackBlockEntityTilt tilt;

    public TrackBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TRACK, pos, state);
        connections = new HashMap<>();
        setLazyTickRate(100);
        tilt = new TrackBlockEntityTilt(this);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        cancelDrops |= world.getBlockState(pos).isOf(AllBlocks.TRACK);
        removeInboundConnections(true);
    }

    public Map<BlockPos, BezierConnection> getConnections() {
        return connections;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (world.isClient && hasInteractableConnections())
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

            if (!key.equals(bc.getKey()) || !pos.equals(bc.bePositions.getFirst())) {
                invalid.add(key);
                continue;
            }

            BlockState blockState = world.getBlockState(key);
            if (blockState.getBlock() instanceof ITrackBlock trackBlock && !blockState.get(TrackBlock.HAS_BE))
                for (Vec3d v : trackBlock.getTrackAxes(world, key, blockState)) {
                    Vec3d bcEndAxis = bc.axes.getSecond();
                    if (v.distanceTo(bcEndAxis) < 1 / 1024f || v.distanceTo(bcEndAxis.multiply(-1)) < 1 / 1024f)
                        world.setBlockState(key, blockState.with(TrackBlock.HAS_BE, true), Block.NOTIFY_ALL);
                }

            BlockEntity blockEntity = world.getBlockEntity(key);
            if (!(blockEntity instanceof TrackBlockEntity trackBE) || blockEntity.isRemoved()) {
                invalid.add(key);
                continue;
            }

            if (!trackBE.connections.containsKey(pos)) {
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
        world.scheduleBlockTick(pos, getCachedState().getBlock(), 1);
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

        if (!connections.isEmpty() || getCachedState().get(TrackBlock.SHAPE, TrackShape.NONE).isPortal())
            return;

        BlockState blockState = world.getBlockState(pos);
        if (blockState.contains(TrackBlock.HAS_BE))
            world.setBlockState(pos, blockState.with(TrackBlock.HAS_BE, false));
        if (world instanceof ServerWorld serverLevel) {
            Packet<?> packet = new RemoveBlockEntityPacket(pos);
            for (ServerPlayerEntity player : serverLevel.getChunkManager().chunkLoadingManager.getPlayersWatchingChunk(new ChunkPos(pos), false)) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    public void removeInboundConnections(boolean dropAndDiscard) {
        for (BezierConnection bezierConnection : connections.values()) {
            if (!(world.getBlockEntity(bezierConnection.getKey()) instanceof TrackBlockEntity tbe))
                return;
            tbe.removeConnection(bezierConnection.bePositions.getFirst());
            if (!dropAndDiscard)
                continue;
            if (!cancelDrops)
                bezierConnection.spawnItems(world);
            bezierConnection.spawnDestroyParticles(world);
        }
        if (dropAndDiscard && world instanceof ServerWorld serverLevel) {
            Packet<?> packet = new RemoveBlockEntityPacket(pos);
            for (ServerPlayerEntity player : serverLevel.getChunkManager().chunkLoadingManager.getPlayersWatchingChunk(new ChunkPos(pos), false)) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    public void bind(RegistryKey<World> boundDimension, BlockPos boundLocation) {
        this.boundLocation = Pair.of(boundDimension, boundLocation);
        markDirty();
    }

    public boolean isTilted() {
        return tilt.smoothingAngle.isPresent();
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeSafe(view);
        writeTurns(view, true);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        writeTurns(view, false);
        tilt.smoothingAngle.ifPresent(angle -> view.put("Smoothing", Codec.DOUBLE, angle));
        if (boundLocation == null)
            return;
        view.put("BoundLocation", BlockPos.CODEC, boundLocation.getSecond());
        view.put("BoundDimension", World.CODEC, boundLocation.getFirst());
    }

    private void writeTurns(WriteView view, boolean restored) {
        WriteView.ListView list = view.getList("Connections");
        for (BezierConnection bezierConnection : connections.values())
            (restored ? tilt.restoreToOriginalCurve(bezierConnection.clone()) : bezierConnection).write(list.add(), pos);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        connections.clear();
        view.getListReadView("Connections").forEach(item -> {
            BezierConnection connection = new BezierConnection(item, pos);
            connections.put(connection.getKey(), connection);
        });

        boolean smoothingPreviously = tilt.smoothingAngle.isPresent();
        tilt.smoothingAngle = view.read("Smoothing", Codec.DOUBLE);
        if (smoothingPreviously != tilt.smoothingAngle.isPresent() && clientPacket) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 16);
        }

        AllClientHandle.INSTANCE.queueUpdate(this);

        if (hasInteractableConnections())
            AllClientHandle.INSTANCE.registerToCurveInteraction(this);
        else
            AllClientHandle.INSTANCE.removeFromCurveInteraction(this);

        view.read("BoundLocation", BlockPos.CODEC)
            .ifPresent(pos -> boundLocation = Pair.of(view.read("BoundDimension", World.CODEC).orElseThrow(), pos));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    public void accept(BlockEntity other) {
        if (other instanceof TrackBlockEntity track)
            connections.putAll(track.connections);
        validateConnections();
        world.scheduleBlockTick(pos, getCachedState().getBlock(), 1);
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
            newConnection.bePositions.setSecond(BlockPos.ofFloored(Vec3d.ofCenter(newConnection.bePositions.getFirst())
                .add(transform.applyWithoutOffsetUncentered(Vec3d.of(diff)))));

            Vec3d beVec = Vec3d.of(pos);
            Vec3d teCenterVec = beVec.add(0.5, 0.5, 0.5);
            Vec3d start = newConnection.starts.getFirst();
            Vec3d startToBE = start.subtract(teCenterVec);
            Vec3d endToStart = newConnection.starts.getSecond().subtract(start);
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
        if (world.isClient)
            AllClientHandle.INSTANCE.removeFromCurveInteraction(this);
    }

    @Override
    public void remove() {
        super.remove();

        for (BezierConnection connection : connections.values())
            manageFakeTracksAlong(connection, true);

        if (boundLocation != null && world instanceof ServerWorld) {
            ServerWorld otherLevel = world.getServer().getWorld(boundLocation.getFirst());
            if (otherLevel == null)
                return;
            if (otherLevel.getBlockState(boundLocation.getSecond()).isIn(AllBlockTags.TRACKS))
                otherLevel.breakBlock(boundLocation.getSecond(), false);
        }
    }

    public void manageFakeTracksAlong(BezierConnection bc, boolean remove) {
        Map<Pair<Integer, Integer>, Double> yLevels = bc.rasterise();

        for (Map.Entry<Pair<Integer, Integer>, Double> entry : yLevels.entrySet()) {
            double yValue = entry.getValue();
            int floor = MathHelper.floor(yValue);
            BlockPos targetPos = new BlockPos(entry.getKey().getFirst(), floor, entry.getKey().getSecond());
            targetPos = targetPos.add(bc.bePositions.getFirst()).up(1);

            BlockState stateAtPos = world.getBlockState(targetPos);
            boolean present = stateAtPos.isOf(AllBlocks.FAKE_TRACK);

            if (remove) {
                if (present)
                    world.removeBlock(targetPos, false);
                continue;
            }

            FluidState fluidState = stateAtPos.getFluidState();
            if (!fluidState.isEmpty() && !fluidState.isEqualAndStill(Fluids.WATER))
                continue;

            if (!present && stateAtPos.isReplaceable())
                world.setBlockState(
                    targetPos,
                    ProperWaterloggedBlock.withWater(world, AllBlocks.FAKE_TRACK.getDefaultState(), targetPos),
                    Block.NOTIFY_ALL
                );
            FakeTrackBlock.keepAlive(world, targetPos);
        }
    }

}
