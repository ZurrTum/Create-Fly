package com.zurrtum.create.foundation.fluid;

import com.mojang.serialization.Codec;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.List;
import java.util.function.Predicate;

public interface FluidIngredient extends Predicate<FluidStack> {
    Codec<FluidIngredient> CODEC = FluidIngredientSerializer.CODEC.dispatch(FluidIngredient::getSerializer, FluidIngredientSerializer::codec);
    PacketCodec<RegistryByteBuf, FluidIngredient> PACKET_CODEC = FluidIngredientSerializer.PACKET_CODEC.dispatch(
        FluidIngredient::getSerializer,
        FluidIngredientSerializer::packetCodec
    );

    int amount();

    boolean test(FluidStack stack);

    List<FluidStack> getMatchingFluidStacks();

    FluidIngredientSerializer getSerializer();
}