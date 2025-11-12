package com.zurrtum.create;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidEntry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFluids {
    public static final List<FlowableFluid> ALL = new ArrayList<>();
    public static final FlowableFluid POTION = register("potion");
    public static final FlowableFluid TEA = register("tea");
    public static final FlowableFluid MILK = register("milk");
    public static final FlowableFluid HONEY = register("honey");
    public static final FlowableFluid CHOCOLATE = register("chocolate");

    private static FlowableFluid register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        ResourceKey<Fluid> still_key = ResourceKey.create(Registries.FLUID, id);
        ResourceKey<Fluid> flowing_key = ResourceKey.create(Registries.FLUID, id.withPrefix("flowing_"));
        FluidEntry entry = new FluidEntry();
        entry.still = new FlowableFluid.Still(entry);
        entry.flowing = new FlowableFluid.Flowing(entry);
        Registry.register(BuiltInRegistries.FLUID, still_key, entry.still);
        Registry.register(BuiltInRegistries.FLUID, flowing_key, entry.flowing);
        ALL.add(entry.still);
        ALL.add(entry.flowing);
        return entry.still;
    }

    public static void register() {
    }
}
