package com.zurrtum.create.foundation.fluid;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public record FluidTagIngredient(TagKey<Fluid> tag, int amount) implements FluidIngredient {
    @Override
    public boolean test(FluidStack stack) {
        return stack.isIn(tag);
    }

    @Override
    public List<Fluid> getMatchingFluids() {
        ImmutableList.Builder<Fluid> builder = ImmutableList.builder();
        for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(tag)) {
            Fluid fluid = holder.value();
            if (fluid instanceof FlowingFluid flowing)
                fluid = flowing.getSource();
            builder.add(fluid);
        }
        return builder.build();
    }

    @Override
    public List<FluidStack> getMatchingFluidStacks() {
        ImmutableList.Builder<FluidStack> builder = ImmutableList.builder();
        for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(tag)) {
            Fluid fluid = holder.value();
            if (fluid instanceof FlowingFluid flowing)
                fluid = flowing.getSource();
            builder.add(new FluidStack(fluid, amount));
        }
        return builder.build();
    }

    @Override
    public FluidIngredientSerializer getSerializer() {
        return FluidIngredientSerializer.FLUID_TAG;
    }

    public record Serializer(String type) implements FluidIngredientSerializer {
        public static final MapCodec<FluidTagIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            TagKey.hashedCodec(Registries.FLUID)
                .fieldOf("fluid_tag").forGetter(FluidTagIngredient::tag),
            Codec.INT.optionalFieldOf("amount", 81000).forGetter(FluidTagIngredient::amount)
        ).apply(i, FluidTagIngredient::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, FluidTagIngredient> PACKET_CODEC = StreamCodec.composite(
            TagKey.streamCodec(
                Registries.FLUID), FluidTagIngredient::tag, ByteBufCodecs.INT, FluidTagIngredient::amount, FluidTagIngredient::new
        );

        @Override
        public MapCodec<FluidTagIngredient> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FluidTagIngredient> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
