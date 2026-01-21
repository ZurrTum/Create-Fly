package com.zurrtum.create.content.trains.signal;

import com.google.common.base.Objects;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.signal.SignalBlock.SignalType;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.OverlayState;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class SignalBoundary extends TrackEdgePoint {
    public Couple<Map<BlockPos, Boolean>> blockEntities;
    public Couple<SignalType> types;
    public Couple<@Nullable UUID> groups;
    public Couple<Boolean> sidesToUpdate;
    public Couple<SignalState> cachedStates;

    private final Couple<@Nullable Map<UUID, Boolean>> chainedSignals;

    public SignalBoundary() {
        blockEntities = Couple.create(HashMap::new);
        chainedSignals = Couple.create(null, null);
        groups = Couple.create(null, null);
        sidesToUpdate = Couple.create(true, true);
        types = Couple.create(() -> SignalType.ENTRY_SIGNAL);
        cachedStates = Couple.create(() -> SignalState.INVALID);
    }

    public void setGroup(boolean primary, @Nullable UUID groupId) {
        UUID previous = groups.get(primary);

        groups.set(primary, groupId);

        UUID opposite = groups.get(!primary);
        Map<UUID, SignalEdgeGroup> signalEdgeGroups = Create.RAILWAYS.signalEdgeGroups;

        if (opposite != null && signalEdgeGroups.containsKey(opposite)) {
            SignalEdgeGroup oppositeGroup = signalEdgeGroups.get(opposite);
            if (previous != null)
                oppositeGroup.removeAdjacent(previous);
            if (groupId != null)
                oppositeGroup.putAdjacent(groupId);
        }

        if (groupId != null && signalEdgeGroups.containsKey(groupId)) {
            SignalEdgeGroup group = signalEdgeGroups.get(groupId);
            if (opposite != null)
                group.putAdjacent(opposite);
        }
    }

    public void setGroupAndUpdate(TrackNode side, UUID groupId) {
        boolean primary = isPrimary(side);
        setGroup(primary, groupId);
        sidesToUpdate.set(primary, false);
        chainedSignals.set(primary, null);
    }

    @Override
    public boolean canMerge() {
        return true;
    }

    @Override
    public void invalidate(LevelAccessor level) {
        blockEntities.forEach(s -> s.keySet().forEach(p -> invalidateAt(level, p)));
        MinecraftServer server = level.getServer();
        groups.forEach(uuid -> {
            if (Create.RAILWAYS.signalEdgeGroups.remove(uuid) != null)
                Create.RAILWAYS.sync.edgeGroupRemoved(server, uuid);
        });
    }

    @Override
    public boolean canCoexistWith(EdgePointType<?> otherType, boolean front) {
        return otherType == getType();
    }

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
        Map<BlockPos, Boolean> blockEntitiesOnSide = blockEntities.get(front);
        if (blockEntitiesOnSide.isEmpty())
            blockEntity.getBlockState().getOptionalValue(SignalBlock.TYPE).ifPresent(type -> types.set(front, type));
        blockEntitiesOnSide.put(blockEntity.getBlockPos(), blockEntity instanceof SignalBlockEntity ste && ste.getReportedPower());
    }

    public void updateBlockEntityPower(SignalBlockEntity blockEntity) {
        for (boolean front : Iterate.trueAndFalse)
            blockEntities.get(front).computeIfPresent(blockEntity.getBlockPos(), (p, c) -> blockEntity.getReportedPower());
    }

    @Override
    public void blockEntityRemoved(MinecraftServer server, BlockPos blockEntityPos, boolean front) {
        blockEntities.forEach(s -> s.remove(blockEntityPos));
        if (blockEntities.both(Map::isEmpty))
            removeFromAllGraphs(server);
    }

    @Override
    public void onRemoved(MinecraftServer server, TrackGraph graph) {
        super.onRemoved(server, graph);
        SignalPropagator.onSignalRemoved(server, graph, this);
    }

    public void queueUpdate(TrackNode side) {
        sidesToUpdate.set(isPrimary(side), true);
    }

    @Nullable
    public UUID getGroup(TrackNode side) {
        return groups.get(isPrimary(side));
    }

    @Override
    public boolean canNavigateVia(TrackNode side) {
        return !blockEntities.get(isPrimary(side)).isEmpty();
    }

    public OverlayState getOverlayFor(BlockPos blockEntity) {
        for (boolean first : Iterate.trueAndFalse) {
            Map<BlockPos, Boolean> set = blockEntities.get(first);
            for (BlockPos blockPos : set.keySet()) {
                if (blockPos.equals(blockEntity))
                    return blockEntities.get(!first).isEmpty() ? OverlayState.RENDER : OverlayState.DUAL;
                return OverlayState.SKIP;
            }
        }
        return OverlayState.SKIP;
    }

    public SignalType getTypeFor(BlockPos blockEntity) {
        return types.get(blockEntities.getFirst().containsKey(blockEntity));
    }

    public SignalState getStateFor(BlockPos blockEntity) {
        for (boolean first : Iterate.trueAndFalse) {
            Map<BlockPos, Boolean> set = blockEntities.get(first);
            if (set.containsKey(blockEntity))
                return cachedStates.get(first);
        }
        return SignalState.INVALID;
    }

    @Override
    public void tick(MinecraftServer server, TrackGraph graph, boolean preTrains) {
        super.tick(server, graph, preTrains);
        if (!preTrains) {
            tickState(server, graph);
            return;
        }
        for (boolean front : Iterate.trueAndFalse) {
            if (!sidesToUpdate.get(front))
                continue;
            sidesToUpdate.set(front, false);
            SignalPropagator.propagateSignalGroup(server, graph, this, front);
            chainedSignals.set(front, null);
        }
    }

    private void tickState(MinecraftServer server, TrackGraph graph) {
        for (boolean current : Iterate.trueAndFalse) {
            Map<BlockPos, Boolean> set = blockEntities.get(current);
            if (set.isEmpty())
                continue;

            boolean forcedRed = isForcedRed(current);
            UUID group = groups.get(current);
            if (Objects.equal(group, groups.get(!current))) {
                cachedStates.set(current, SignalState.INVALID);
                continue;
            }

            Map<UUID, SignalEdgeGroup> signalEdgeGroups = Create.RAILWAYS.signalEdgeGroups;
            SignalEdgeGroup signalEdgeGroup = signalEdgeGroups.get(group);
            if (signalEdgeGroup == null) {
                cachedStates.set(current, SignalState.INVALID);
                continue;
            }

            boolean occupiedUnlessBySelf = forcedRed || signalEdgeGroup.isOccupiedUnless(this);
            cachedStates.set(current, occupiedUnlessBySelf ? SignalState.RED : resolveSignalChain(server, graph, current));
        }
    }

    public boolean isForcedRed(TrackNode side) {
        return isForcedRed(isPrimary(side));
    }

    public boolean isForcedRed(boolean primary) {
        Collection<Boolean> values = blockEntities.get(primary).values();
        for (Boolean b : values)
            if (b)
                return true;
        return false;
    }

    private SignalState resolveSignalChain(MinecraftServer server, TrackGraph graph, boolean side) {
        if (types.get(side) != SignalType.CROSS_SIGNAL)
            return SignalState.GREEN;

        if (chainedSignals.get(side) == null)
            chainedSignals.set(side, SignalPropagator.collectChainedSignals(server, graph, this, side));

        boolean allPathsFree = true;
        boolean noPathsFree = true;
        boolean invalid = false;

        for (Map.Entry<UUID, Boolean> entry : chainedSignals.get(side).entrySet()) {
            UUID uuid = entry.getKey();
            boolean sideOfOther = entry.getValue();
            SignalBoundary otherSignal = graph.getPoint(EdgePointType.SIGNAL, uuid);
            if (otherSignal == null) {
                invalid = true;
                break;
            }
            if (otherSignal.blockEntities.get(sideOfOther).isEmpty())
                continue;
            SignalState otherState = otherSignal.cachedStates.get(sideOfOther);
            allPathsFree &= otherState == SignalState.GREEN || otherState == SignalState.INVALID;
            noPathsFree &= otherState == SignalState.RED;
        }
        if (invalid) {
            chainedSignals.set(side, null);
            return SignalState.INVALID;
        }
        if (allPathsFree)
            return SignalState.GREEN;
        if (noPathsFree)
            return SignalState.RED;
        return SignalState.YELLOW;
    }

    @Override
    public void read(ValueInput view, boolean migration, DimensionPalette dimensions) {
        super.read(view, migration, dimensions);

        if (migration)
            return;

        blockEntities = Couple.create(HashMap::new);
        groups = Couple.create(null, null);

        for (int i = 1; i <= 2; i++) {
            boolean first = i == 1;
            view.read("Tiles" + i, CreateCodecs.BLOCK_POS_BOOLEAN_MAP_CODEC).ifPresent(map -> blockEntities.set(first, map));
        }

        for (int i = 1; i <= 2; i++) {
            boolean first = i == 1;
            view.read("Group" + i, UUIDUtil.CODEC).ifPresent(uuid -> groups.set(first, uuid));
        }
        for (int i = 1; i <= 2; i++)
            sidesToUpdate.set(i == 1, view.getBooleanOr("Update" + i, false));
        for (int i = 1; i <= 2; i++)
            types.set(i == 1, view.read("Type" + i, SignalType.CODEC).orElse(SignalType.ENTRY_SIGNAL));
        for (int i = 1; i <= 2; i++)
            cachedStates.set(i == 1, view.read("State" + i, SignalState.CODEC).orElse(SignalState.RED));
    }

    @Override
    public <T> void decode(DynamicOps<T> ops, T input, boolean migration, DimensionPalette dimensions) {
        super.decode(ops, input, migration, dimensions);

        if (migration)
            return;

        blockEntities = Couple.create(HashMap::new);
        groups = Couple.create(null, null);

        MapLike<T> map = ops.getMap(input).getOrThrow();
        for (int i = 1; i <= 2; i++) {
            boolean first = i == 1;
            Optional.ofNullable(map.get("Tiles" + i)).flatMap(value -> CreateCodecs.BLOCK_POS_BOOLEAN_MAP_CODEC.parse(ops, value).result())
                .ifPresent(value -> blockEntities.set(first, value));
        }

        for (int i = 1; i <= 2; i++) {
            boolean first = i == 1;
            Optional.ofNullable(map.get("Group" + i)).flatMap(value -> UUIDUtil.CODEC.parse(ops, value).result())
                .ifPresent(uuid -> groups.set(first, uuid));
        }
        for (int i = 1; i <= 2; i++)
            sidesToUpdate.set(i == 1, Optional.ofNullable(map.get("Update" + i)).map(value -> ops.getBooleanValue(value).getOrThrow()).orElse(false));
        for (int i = 1; i <= 2; i++)
            types.set(i == 1, SignalType.CODEC.parse(ops, map.get("Type" + i)).result().orElse(SignalType.ENTRY_SIGNAL));
        for (int i = 1; i <= 2; i++)
            cachedStates.set(i == 1, SignalState.CODEC.parse(ops, map.get("State" + i)).result().orElse(SignalState.RED));
    }

    @Override
    public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
        super.read(buffer, dimensions);
        for (int i = 1; i <= 2; i++) {
            if (buffer.readBoolean())
                groups.set(i == 1, buffer.readUUID());
        }
    }

    @Override
    public void write(ValueOutput view, DimensionPalette dimensions) {
        super.write(view, dimensions);
        for (int i = 1; i <= 2; i++)
            if (!blockEntities.get(i == 1).isEmpty())
                view.store("Tiles" + i, CreateCodecs.BLOCK_POS_BOOLEAN_MAP_CODEC, blockEntities.get(i == 1));
        for (int i = 1; i <= 2; i++)
            if (groups.get(i == 1) != null)
                view.store("Group" + i, UUIDUtil.CODEC, groups.get(i == 1));
        for (int i = 1; i <= 2; i++)
            if (sidesToUpdate.get(i == 1))
                view.putBoolean("Update" + i, true);
        for (int i = 1; i <= 2; i++)
            view.store("Type" + i, SignalType.CODEC, types.get(i == 1));
        for (int i = 1; i <= 2; i++)
            view.store("State" + i, SignalState.CODEC, cachedStates.get(i == 1));
    }

    @Override
    public <T> DataResult<T> encode(DynamicOps<T> ops, T empty, DimensionPalette dimensions) {
        DataResult<T> prefix = super.encode(ops, empty, dimensions);
        RecordBuilder<T> map = ops.mapBuilder();
        for (int i = 1; i <= 2; i++)
            if (!blockEntities.get(i == 1).isEmpty())
                map.add("Tiles" + i, blockEntities.get(i == 1), CreateCodecs.BLOCK_POS_BOOLEAN_MAP_CODEC);
        for (int i = 1; i <= 2; i++)
            if (groups.get(i == 1) != null)
                map.add("Group" + i, groups.get(i == 1), UUIDUtil.CODEC);
        for (int i = 1; i <= 2; i++)
            if (sidesToUpdate.get(i == 1))
                map.add("Update" + i, ops.createBoolean(true));
        for (int i = 1; i <= 2; i++)
            map.add("Type" + i, types.get(i == 1), SignalType.CODEC);
        for (int i = 1; i <= 2; i++)
            map.add("State" + i, cachedStates.get(i == 1), SignalState.CODEC);
        return map.build(prefix);
    }

    @Override
    public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
        super.write(buffer, dimensions);
        for (int i = 1; i <= 2; i++) {
            boolean hasGroup = groups.get(i == 1) != null;
            buffer.writeBoolean(hasGroup);
            if (hasGroup)
                buffer.writeUUID(groups.get(i == 1));
        }
    }

    public void cycleSignalType(BlockPos pos) {
        types.set(blockEntities.getFirst().containsKey(pos), SignalType.values()[(getTypeFor(pos).ordinal() + 1) % SignalType.values().length]);
    }

}