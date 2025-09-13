package com.zurrtum.create.foundation.fluid;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

import java.util.List;

public record FluidTagIngredient(TagKey<Fluid> tag, int amount) implements FluidIngredient {
    @Override
    public boolean test(FluidStack stack) {
        return stack.isIn(tag);
    }

    @Override
    public List<FluidStack> getMatchingFluidStacks() {
        ImmutableList.Builder<FluidStack> builder = ImmutableList.builder();
        for (RegistryEntry<Fluid> holder : Registries.FLUID.iterateEntries(tag)) {
            Fluid fluid = holder.value();
            if (fluid instanceof FlowableFluid flowing)
                fluid = flowing.getStill();
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
            TagKey.codec(RegistryKeys.FLUID).fieldOf("fluid_tag").forGetter(FluidTagIngredient::tag),
            Codec.INT.optionalFieldOf("amount", 81000).forGetter(FluidTagIngredient::amount)
        ).apply(i, FluidTagIngredient::new));
        public static final PacketCodec<RegistryByteBuf, FluidTagIngredient> PACKET_CODEC = PacketCodec.tuple(
            TagKey.packetCodec(RegistryKeys.FLUID),
            FluidTagIngredient::tag,
            PacketCodecs.INTEGER,
            FluidTagIngredient::amount,
            FluidTagIngredient::new
        );

        @Override
        public MapCodec<FluidTagIngredient> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, FluidTagIngredient> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
