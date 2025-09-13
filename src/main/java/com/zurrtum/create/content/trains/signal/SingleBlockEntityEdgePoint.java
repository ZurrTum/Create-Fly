package com.zurrtum.create.content.trains.signal;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public abstract class SingleBlockEntityEdgePoint extends TrackEdgePoint {

    public RegistryKey<World> blockEntityDimension;
    public BlockPos blockEntityPos;

    public BlockPos getBlockEntityPos() {
        return blockEntityPos;
    }

    public RegistryKey<World> getBlockEntityDimension() {
        return blockEntityDimension;
    }

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
        this.blockEntityPos = blockEntity.getPos();
        this.blockEntityDimension = blockEntity.getWorld().getRegistryKey();
    }

    @Override
    public void blockEntityRemoved(MinecraftServer server, BlockPos blockEntityPos, boolean front) {
        removeFromAllGraphs(server);
    }

    @Override
    public void invalidate(WorldAccess level) {
        invalidateAt(level, blockEntityPos);
    }

    @Override
    public boolean canMerge() {
        return false;
    }

    @Override
    public void read(ReadView view, boolean migration, DimensionPalette dimensions) {
        super.read(view, migration, dimensions);
        if (migration)
            return;
        blockEntityPos = view.read("BlockEntityPos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        blockEntityDimension = view.read("BlockEntityDimension", dimensions).orElseThrow();
    }

    @Override
    public <T> void decode(DynamicOps<T> ops, T input, boolean migration, DimensionPalette dimensions) {
        super.decode(ops, input, migration, dimensions);
        if (migration)
            return;
        MapLike<T> map = ops.getMap(input).getOrThrow();
        blockEntityPos = BlockPos.CODEC.parse(ops, map.get("BlockEntityPos")).result().orElse(BlockPos.ORIGIN);
        blockEntityDimension = dimensions.parse(ops, map.get("BlockEntityDimension")).getOrThrow();
    }

    @Override
    public void write(WriteView view, DimensionPalette dimensions) {
        super.write(view, dimensions);
        view.put("BlockEntityPos", BlockPos.CODEC, blockEntityPos);
        view.put("BlockEntityDimension", dimensions, blockEntityDimension);
    }

    @Override
    public <T> DataResult<T> encode(DynamicOps<T> ops, T empty, DimensionPalette dimensions) {
        DataResult<T> prefix = super.encode(ops, empty, dimensions);
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("BlockEntityPos", blockEntityPos, BlockPos.CODEC);
        map.add("BlockEntityDimension", blockEntityDimension, dimensions);
        return map.build(prefix);
    }
}
