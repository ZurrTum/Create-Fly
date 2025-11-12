package com.zurrtum.create.client.ponder.foundation.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;

public class PonderChunkSection extends LevelChunkSection {
    public final PonderChunk owner;
    protected final BlockPos.MutableBlockPos scratchPos;

    public final int xStart;
    public final int yStart;
    public final int zStart;
    public boolean empty;

    public PonderChunkSection(PonderChunk owner, BlockPos.MutableBlockPos scratchPos, ChunkPos pos, int yBase, boolean hasBlock) {
        super(owner.world.palettedContainerFactory());
        this.owner = owner;
        this.scratchPos = scratchPos;
        this.xStart = pos.getMinBlockX();
        this.yStart = yBase;
        this.zStart = pos.getMinBlockZ();
        this.empty = !hasBlock;
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        // ChunkSection#getBlockState expects local chunk coordinates, so we add to get
        // back into world coords.
        return owner.world.getBlockState(scratchPos.set(x + xStart, y + yStart, z + zStart));
    }

    @Override
    public FluidState getFluidState(int x, int y, int z) {
        return getBlockState(x, y, z).getFluidState();
    }

    @Override
    public BlockState setBlockState(int x, int y, int z, BlockState state, boolean useLocks) {
        throw new UnsupportedOperationException("Chunk sections cannot be mutated in a fake world.");
    }

    @Override
    public boolean hasOnlyAir() {
        return empty;
    }
}