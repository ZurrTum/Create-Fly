package com.zurrtum.create.foundation.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface FluidIngredientSerializer {
    Map<String, FluidIngredientSerializer> REGISTRY = new HashMap<>();
    Codec<FluidIngredientSerializer> CODEC = Codec.STRING.xmap(REGISTRY::get, FluidIngredientSerializer::type);
    StreamCodec<RegistryFriendlyByteBuf, FluidIngredientSerializer> PACKET_CODEC = StreamCodec.ofMember(
        (serializer, buf) -> buf.writeUtf(serializer.type()),
        buf -> REGISTRY.get(buf.readUtf())
    );
    FluidIngredientSerializer FLUID_STACK = register("fluid_stack", FluidStackIngredient.Serializer::new);
    FluidIngredientSerializer FLUID_TAG = register("fluid_tag", FluidTagIngredient.Serializer::new);

    String type();

    MapCodec<? extends FluidIngredient> codec();

    StreamCodec<RegistryFriendlyByteBuf, ? extends FluidIngredient> packetCodec();

    static FluidIngredientSerializer register(String type, Function<String, FluidIngredientSerializer> factory) {
        FluidIngredientSerializer serializer = factory.apply(type);
        REGISTRY.put(type, serializer);
        return serializer;
    }
}
