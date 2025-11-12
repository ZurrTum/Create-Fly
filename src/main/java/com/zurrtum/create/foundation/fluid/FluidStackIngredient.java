package com.zurrtum.create.foundation.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.List;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

public record FluidStackIngredient(Fluid fluid, DataComponentPatch components, int amount) implements FluidIngredient {
    @Override
    public boolean test(FluidStack stack) {
        if (stack.getFluid() != fluid)
            return false;
        if (components.isEmpty())
            return true;
        return stack.getComponentChanges().map.reference2ObjectEntrySet()
            .containsAll(components.map.reference2ObjectEntrySet());
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
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidStackIngredient::fluid),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStackIngredient::components),
            Codec.INT.optionalFieldOf("amount", 81000).forGetter(FluidStackIngredient::amount)
        ).apply(instance, FluidStackIngredient::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackIngredient> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.FLUID),
            FluidStackIngredient::fluid,
            DataComponentPatch.STREAM_CODEC,
            FluidStackIngredient::components,
            ByteBufCodecs.INT,
            FluidStackIngredient::amount,
            FluidStackIngredient::new
        );

        @Override
        public MapCodec<FluidStackIngredient> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FluidStackIngredient> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
