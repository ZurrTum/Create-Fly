package com.zurrtum.create.content.fluids.pipes;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.infrastructure.fluids.BottleFluidInventory;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class VanillaFluidTargets {

    public static boolean canProvideFluidWithoutCapability(BlockState state) {
        if (state.hasProperty(BlockStateProperties.LEVEL_HONEY))
            return true;
        if (state.is(Blocks.CAULDRON))
            return true;
        if (state.is(Blocks.LAVA_CAULDRON))
            return true;
        return state.is(Blocks.WATER_CAULDRON);
    }

    public static FluidStack drainBlock(Level level, BlockPos pos, BlockState state, boolean simulate) {
        if (state.hasProperty(BlockStateProperties.LEVEL_HONEY) && state.getValue(BlockStateProperties.LEVEL_HONEY) >= 5) {
            if (!simulate)
                level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_HONEY, 0), Block.UPDATE_ALL);
            return new FluidStack(AllFluids.HONEY, BottleFluidInventory.CAPACITY);
        }

        if (state.is(Blocks.LAVA_CAULDRON)) {
            if (!simulate)
                level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
            return new FluidStack(Fluids.LAVA, BucketFluidInventory.CAPACITY);
        }

        if (state.is(Blocks.WATER_CAULDRON) && state.getBlock() instanceof LayeredCauldronBlock lcb) {
            if (!lcb.isFull(state))
                return FluidStack.EMPTY;
            if (!simulate)
                level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
            return new FluidStack(Fluids.WATER, BucketFluidInventory.CAPACITY);
        }

        return FluidStack.EMPTY;
    }

}
