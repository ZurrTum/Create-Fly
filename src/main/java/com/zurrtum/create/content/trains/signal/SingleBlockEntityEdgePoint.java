package com.zurrtum.create.content.trains.signal;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class SingleBlockEntityEdgePoint extends TrackEdgePoint {

    public ResourceKey<Level> blockEntityDimension;
    public @Nullable BlockPos blockEntityPos;

    @Nullable
    public BlockPos getBlockEntityPos() {
        return blockEntityPos;
    }

    public ResourceKey<Level> getBlockEntityDimension() {
        return blockEntityDimension;
    }

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
        this.blockEntityPos = blockEntity.getBlockPos();
        this.blockEntityDimension = blockEntity.getLevel().dimension();
    }

    @Override
    public void blockEntityRemoved(MinecraftServer server, BlockPos blockEntityPos, boolean front) {
        removeFromAllGraphs(server);
    }

    @Override
    public void invalidate(LevelAccessor level) {
        invalidateAt(level, blockEntityPos);
    }

    @Override
    public boolean canMerge() {
        return false;
    }

    @Override
    public void read(ValueInput view, boolean migration, DimensionPalette dimensions) {
        super.read(view, migration, dimensions);
        if (migration)
            return;
        blockEntityPos = view.read("BlockEntityPos", BlockPos.CODEC).orElse(BlockPos.ZERO);
        blockEntityDimension = view.read("BlockEntityDimension", dimensions).orElseThrow();
    }

    @Override
    public <T> void decode(DynamicOps<T> ops, T input, boolean migration, DimensionPalette dimensions) {
        super.decode(ops, input, migration, dimensions);
        if (migration)
            return;
        MapLike<T> map = ops.getMap(input).getOrThrow();
        blockEntityPos = BlockPos.CODEC.parse(ops, map.get("BlockEntityPos")).result().orElse(BlockPos.ZERO);
        blockEntityDimension = dimensions.parse(ops, map.get("BlockEntityDimension")).getOrThrow();
    }

    @Override
    public void write(ValueOutput view, DimensionPalette dimensions) {
        super.write(view, dimensions);
        view.store("BlockEntityPos", BlockPos.CODEC, blockEntityPos);
        view.store("BlockEntityDimension", dimensions, blockEntityDimension);
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
