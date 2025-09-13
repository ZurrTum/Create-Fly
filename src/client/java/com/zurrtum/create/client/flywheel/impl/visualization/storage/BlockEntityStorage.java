package com.zurrtum.create.client.flywheel.impl.visualization.storage;

import com.zurrtum.create.client.flywheel.api.visual.BlockEntityVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockEntityStorage extends Storage<BlockEntity> {
    private final Long2ObjectMap<BlockEntityVisual<?>> posLookup = new Long2ObjectOpenHashMap<>();

    @Nullable
    public BlockEntityVisual<?> visualAtPos(long pos) {
        return posLookup.get(pos);
    }

    @Override
    public boolean willAccept(BlockEntity blockEntity) {
        if (blockEntity.isRemoved()) {
            return false;
        }

        if (!VisualizationHelper.canVisualize(blockEntity)) {
            return false;
        }

        World level = blockEntity.getWorld();
        if (level == null) {
            return false;
        }

        if (level.isAir(blockEntity.getPos())) {
            return false;
        }

        BlockPos pos = blockEntity.getPos();
        BlockView existingChunk = level.getChunkAsView(pos.getX() >> 4, pos.getZ() >> 4);
        return existingChunk != null;
    }

    @Override
    @Nullable
    protected BlockEntityVisual<?> createRaw(VisualizationContext visualizationContext, BlockEntity obj, float partialTick) {
        var visualizer = VisualizationHelper.getVisualizer(obj);
        if (visualizer == null) {
            return null;
        }

        var visual = visualizer.createVisual(visualizationContext, obj, partialTick);

        BlockPos blockPos = obj.getPos();
        posLookup.put(blockPos.asLong(), visual);

        return visual;
    }

    @Override
    public void remove(BlockEntity obj) {
        posLookup.remove(obj.getPos().asLong());
        super.remove(obj);
    }

    @Override
    public void recreateAll(VisualizationContext visualizationContext, float partialTick) {
        posLookup.clear();
        super.recreateAll(visualizationContext, partialTick);
    }

    @Override
    public void invalidate() {
        posLookup.clear();
        super.invalidate();
    }
}
