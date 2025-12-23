package com.zurrtum.create.api.behaviour.spouting;

import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class CauldronSpoutingBehavior implements BlockSpoutingBehaviour {
    public static final SimpleRegistry<Fluid, CauldronInfo> CAULDRON_INFO = Util.make(() -> {
        SimpleRegistry<Fluid, CauldronInfo> registry = SimpleRegistry.create();
        registry.register(Fluids.WATER, new CauldronInfo(27000, Blocks.WATER_CAULDRON));
        registry.register(Fluids.LAVA, new CauldronInfo(81000, Blocks.LAVA_CAULDRON));
        return registry;
    });

    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        CauldronInfo info = CAULDRON_INFO.get(availableFluid.getFluid());
        if (info == null)
            return 0;

        if (availableFluid.getAmount() < info.amount)
            return 0;

        if (!simulate) {
            level.setBlockAndUpdate(pos, info.cauldron);
        }

        return info.amount;
    }

    /**
     * @param amount   the amount of fluid that must be inserted into an empty cauldron
     * @param cauldron the BlockState to set after filling an empty cauldron with the given amount of fluid
     */
    public record CauldronInfo(int amount, BlockState cauldron) {
        public CauldronInfo(int amount, Block block) {
            this(amount, block.defaultBlockState());
        }
    }
}
