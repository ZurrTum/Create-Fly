package com.zurrtum.create.client.foundation.utility.worldWrappers;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;

public class WrappedBlockAndTintGetter implements BlockRenderView {
    protected final BlockRenderView wrapped;

    public WrappedBlockAndTintGetter(BlockRenderView wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return wrapped.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return wrapped.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return wrapped.getFluidState(pos);
    }

    @Override
    public int getHeight() {
        return wrapped.getHeight();
    }

    @Override
    public int getBottomY() {
        return wrapped.getBottomY();
    }

    @Override
    public float getBrightness(Direction pDirection, boolean pShade) {
        return wrapped.getBrightness(pDirection, pShade);
    }

    @Override
    public LightingProvider getLightingProvider() {
        return wrapped.getLightingProvider();
    }

    @Override
    public int getColor(BlockPos pBlockPos, ColorResolver pColorResolver) {
        return wrapped.getColor(pBlockPos, pColorResolver);
    }

}
