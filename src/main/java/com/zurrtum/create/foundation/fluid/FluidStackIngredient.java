package com.zurrtum.create.foundation.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;

import java.util.List;

public record FluidStackIngredient(Fluid fluid, ComponentChanges components, int amount) implements FluidIngredient {
    @Override
    public boolean test(FluidStack stack) {
        if (stack.getFluid() != fluid)
            return false;
        if (components.isEmpty())
            return true;
        return stack.getComponentChanges().changedComponents.reference2ObjectEntrySet()
            .containsAll(components.changedComponents.reference2ObjectEntrySet());
    }

    @Override
    public List<Fluid> getMatchingFluids() {
        return List.of(fluid);
    }

    @Override
    public List<FluidStack> getMatchingFluidStacks() {
        return List.of(new FluidStack(fluid, amount, components));
    }

    @Override
    public FluidIngredientSerializer getSerializer() {
        return FluidIngredientSerializer.FLUID_STACK;
    }

    public record Serializer(String type) implements FluidIngredientSerializer {
        public static final MapCodec<FluidStackIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Registries.FLUID.getCodec().fieldOf("fluid").forGetter(FluidStackIngredient::fluid),
            ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(FluidStackIngredient::components),
            Codec.INT.optionalFieldOf("amount", 81000).forGetter(FluidStackIngredient::amount)
        ).apply(instance, FluidStackIngredient::new));
        public static final PacketCodec<RegistryByteBuf, FluidStackIngredient> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryValue(RegistryKeys.FLUID),
            FluidStackIngredient::fluid,
            ComponentChanges.PACKET_CODEC,
            FluidStackIngredient::components,
            PacketCodecs.INTEGER,
            FluidStackIngredient::amount,
            FluidStackIngredient::new
        );

        @Override
        public MapCodec<FluidStackIngredient> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, FluidStackIngredient> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
