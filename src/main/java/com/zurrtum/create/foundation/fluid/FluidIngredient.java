package com.zurrtum.create.foundation.fluid;

import com.mojang.serialization.Codec;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.function.Predicate;

public interface FluidIngredient extends Predicate<FluidStack> {
    Codec<FluidIngredient> CODEC = FluidIngredientSerializer.CODEC.dispatch(
        FluidIngredient::getSerializer,
        FluidIngredientSerializer::codec
    );
    StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> PACKET_CODEC = FluidIngredientSerializer.PACKET_CODEC.dispatch(FluidIngredient::getSerializer,
        FluidIngredientSerializer::packetCodec
    );

    int amount();

    boolean test(FluidStack stack);

    List<Fluid> getMatchingFluids();

    List<FluidStack> getMatchingFluidStacks();

    FluidIngredientSerializer getSerializer();
}