package com.zurrtum.create.content.fluids.tank;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.boiler.BoilerHeater;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BoilerHeaters {
    public static void register() {
        BoilerHeater.REGISTRY.register(AllBlocks.BLAZE_BURNER, BoilerHeater.BLAZE_BURNER);
        BoilerHeater.REGISTRY.registerProvider(SimpleRegistry.Provider.forBlockTag(AllBlockTags.PASSIVE_BOILER_HEATERS, BoilerHeater.PASSIVE));
    }

    public static int passive(Level level, BlockPos pos, BlockState state) {
        return BlockHelper.isNotUnheated(state) ? BoilerHeater.PASSIVE_HEAT : BoilerHeater.NO_HEAT;
    }

    public static int blazeBurner(Level level, BlockPos pos, BlockState state) {
        HeatLevel value = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
        if (value == HeatLevel.NONE) {
            return BoilerHeater.NO_HEAT;
        }
        if (value == HeatLevel.SEETHING) {
            return 2;
        }
        if (value.isAtLeast(HeatLevel.FADING)) {
            return 1;
        }
        return BoilerHeater.PASSIVE_HEAT;
    }
}
