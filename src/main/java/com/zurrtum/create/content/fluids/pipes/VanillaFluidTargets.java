package com.zurrtum.create.content.fluids.pipes;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.infrastructure.fluids.BottleFluidInventory;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VanillaFluidTargets {

    public static boolean canProvideFluidWithoutCapability(BlockState state) {
        if (state.contains(Properties.HONEY_LEVEL))
            return true;
        if (state.isOf(Blocks.CAULDRON))
            return true;
        if (state.isOf(Blocks.LAVA_CAULDRON))
            return true;
        return state.isOf(Blocks.WATER_CAULDRON);
    }

    public static FluidStack drainBlock(World level, BlockPos pos, BlockState state, boolean simulate) {
        if (state.contains(Properties.HONEY_LEVEL) && state.get(Properties.HONEY_LEVEL) >= 5) {
            if (!simulate)
                level.setBlockState(pos, state.with(Properties.HONEY_LEVEL, 0), Block.NOTIFY_ALL);
            return new FluidStack(AllFluids.HONEY, BottleFluidInventory.CAPACITY);
        }

        if (state.isOf(Blocks.LAVA_CAULDRON)) {
            if (!simulate)
                level.setBlockState(pos, Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
            return new FluidStack(Fluids.LAVA, BucketFluidInventory.CAPACITY);
        }

        if (state.isOf(Blocks.WATER_CAULDRON) && state.getBlock() instanceof LeveledCauldronBlock lcb) {
            if (!lcb.isFull(state))
                return FluidStack.EMPTY;
            if (!simulate)
                level.setBlockState(pos, Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
            return new FluidStack(Fluids.WATER, BucketFluidInventory.CAPACITY);
        }

        return FluidStack.EMPTY;
    }

}
