package com.zurrtum.create.api.effect;

import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * Interface for custom behavior for fluids spilling out of open pipes. Examples:
 * <ul>
 *     <li>Potions: applying potion effects</li>
 *     <li>Milk: clearing effects</li>
 *     <li>Water: extinguishing fire</li>
 * </ul>
 */
@FunctionalInterface
public interface OpenPipeEffectHandler {
    SimpleRegistry<Fluid, OpenPipeEffectHandler> REGISTRY = SimpleRegistry.create();

    /**
     * @param area  the area to apply effects in
     * @param fluid the fluid in the pipe. Do not modify, it will do nothing
     */
    void apply(World level, Box area, FluidStack fluid);
}
