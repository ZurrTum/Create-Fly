package com.zurrtum.create.client.ponder.foundation.level;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;

public class PonderChunkSection extends ChunkSection {
    public final PonderChunk owner;
    protected final BlockPos.Mutable scratchPos;

    public final int xStart;
    public final int yStart;
    public final int zStart;
    public boolean empty;

    public PonderChunkSection(PonderChunk owner, BlockPos.Mutable scratchPos, ChunkPos pos, int yBase, boolean hasBlock) {
        super(owner.world.getPalettesFactory());
        this.owner = owner;
        this.scratchPos = scratchPos;
        this.xStart = pos.getStartX();
        this.yStart = yBase;
        this.zStart = pos.getStartZ();
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
    public boolean isEmpty() {
        return empty;
    }
}