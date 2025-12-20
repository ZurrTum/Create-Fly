package com.zurrtum.create;

import com.zurrtum.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.zurrtum.create.api.behaviour.spouting.CauldronSpoutingBehavior;
import com.zurrtum.create.api.behaviour.spouting.StateChangingBehavior;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

public class AllBlockSpoutingBehaviours {
    public static final BlockSpoutingBehaviour MUD = StateChangingBehavior.setTo(250, AllBlockSpoutingBehaviours::isWater, Blocks.MUD);
    public static final BlockSpoutingBehaviour FARMLAND = StateChangingBehavior.incrementingState(
        8100,
        AllBlockSpoutingBehaviours::isWater,
        FarmlandBlock.MOISTURE
    );
    public static final BlockSpoutingBehaviour WATER_CAULDRON = StateChangingBehavior.incrementingState(
        27000,
        AllBlockSpoutingBehaviours::isWater,
        LeveledCauldronBlock.LEVEL
    );
    public static final CauldronSpoutingBehavior CAULDRON = new CauldronSpoutingBehavior();

    private static boolean isWater(Fluid fluid) {
        return fluid.matchesType(Fluids.WATER);
    }

    public static void register() {
        BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.DIRT, MUD);
        BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.COARSE_DIRT, MUD);
        BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.ROOTED_DIRT, MUD);
        BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.FARMLAND, FARMLAND);
        BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.WATER_CAULDRON, WATER_CAULDRON);
        BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.CAULDRON, CAULDRON);
    }
}
