package com.zurrtum.create.api.boiler;

import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.fluids.tank.BoilerHeaters;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A BoilerHeater provides heat to boilers.
 * Boilers will query blocks for heaters through the registry, usually with {@link #findHeat(Level, BlockPos, BlockState) findHeat}.
 * Heaters can provide a heat level by returning any positive integer from their {@link #getHeat(Level, BlockPos, BlockState) getHeat} method.
 * Returning any negative number counts as no heat - {@link #NO_HEAT} is provided for convenience.
 * <p>
 * Returning {@link #PASSIVE_HEAT} is special - passive heat can be used to provide a small amount of heat, highly limiting
 * in its abilities. This is usually used for free sources of heat, such as fire or magma blocks.
 */
@FunctionalInterface
public interface BoilerHeater {
    int PASSIVE_HEAT = 0;
    int NO_HEAT = -1;

    /**
     * The heater used by common passively-heating blocks. Automatically provides
     * heat for any block in the {@code create:passive_boiler_heaters} block tag.
     */
    BoilerHeater PASSIVE = BoilerHeaters::passive;
    /**
     * The heater used by Blaze Burners. Addons can register this to their own blocks if they use the same functionality.
     */
    BoilerHeater BLAZE_BURNER = BoilerHeaters::blazeBurner;

    SimpleRegistry<Block, BoilerHeater> REGISTRY = SimpleRegistry.create();

    /**
     * Gets the heat at the given location. If a heater is present, queries it for heat. If not, returns {@link #NO_HEAT}.
     */
    static float findHeat(Level level, BlockPos pos, BlockState state) {
        BoilerHeater heater = REGISTRY.get(state);
        return heater != null ? heater.getHeat(level, pos, state) : NO_HEAT;
    }

    /**
     * @return the amount of heat to provide.
     * @see #NO_HEAT
     * @see #PASSIVE_HEAT
     */
    float getHeat(Level level, BlockPos pos, BlockState state);
}
