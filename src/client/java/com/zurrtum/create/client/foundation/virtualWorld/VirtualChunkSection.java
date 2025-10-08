package com.zurrtum.create.client.foundation.virtualWorld;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkSection;

public class VirtualChunkSection extends ChunkSection {
    public final VirtualChunk owner;

    public final int xStart;
    public final int yStart;
    public final int zStart;

    public VirtualChunkSection(VirtualChunk owner, int yBase) {
        super(owner.world.getPalettesFactory());
        this.owner = owner;
        this.xStart = owner.getPos().getStartX();
        this.yStart = yBase;
        this.zStart = owner.getPos().getStartZ();
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        // ChunkSection#getBlockState expects local chunk coordinates, so we add to get
        // back into world coords.
        return owner.world.getBlockState(x + xStart, y + yStart, z + zStart);
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
        ChunkSectionPos sectionPos = ChunkSectionPos.from(xStart >> 4, yStart >> 4, zStart >> 4);
        return owner.world.nonEmptyBlockCounts.getShort(sectionPos) == 0;
    }
}
