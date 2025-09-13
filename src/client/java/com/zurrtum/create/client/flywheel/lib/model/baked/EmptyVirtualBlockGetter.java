package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public class EmptyVirtualBlockGetter extends VirtualBlockGetter {
    public static final EmptyVirtualBlockGetter FULL_DARK = new EmptyVirtualBlockGetter(p -> 0, p -> 0);
    public static final EmptyVirtualBlockGetter FULL_BRIGHT = new EmptyVirtualBlockGetter(p -> 15, p -> 15);

    public EmptyVirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
        super(blockLightFunc, skyLightFunc);
    }

    @Override
    @Nullable
    public final BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public final BlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public final FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.getDefaultState();
    }

    @Override
    public final int getHeight() {
        return 1;
    }

    @Override
    public final int getBottomY() {
        return 0;
    }
}
