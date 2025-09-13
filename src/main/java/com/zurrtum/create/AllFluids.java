package com.zurrtum.create;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidEntry;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFluids {
    public static final List<FlowableFluid> ALL = new ArrayList<>();
    public static final FlowableFluid POTION = register("potion");
    public static final FlowableFluid TEA = register("tea");
    public static final FlowableFluid MILK = register("milk");
    public static final FlowableFluid HONEY = register("honey");
    public static final FlowableFluid CHOCOLATE = register("chocolate");

    private static FlowableFluid register(String name) {
        Identifier id = Identifier.of(MOD_ID, name);
        RegistryKey<Fluid> still_key = RegistryKey.of(RegistryKeys.FLUID, id);
        RegistryKey<Fluid> flowing_key = RegistryKey.of(RegistryKeys.FLUID, id.withPrefixedPath("flowing_"));
        FluidEntry entry = new FluidEntry();
        entry.still = new FlowableFluid.Still(entry);
        entry.flowing = new FlowableFluid.Flowing(entry);
        Registry.register(Registries.FLUID, still_key, entry.still);
        Registry.register(Registries.FLUID, flowing_key, entry.flowing);
        ALL.add(entry.still);
        ALL.add(entry.flowing);
        return entry.still;
    }

    public static void register() {
    }
}
