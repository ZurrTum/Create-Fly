package com.zurrtum.create.foundation.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface FluidIngredientSerializer {
    Map<String, FluidIngredientSerializer> REGISTRY = new HashMap<>();
    Codec<FluidIngredientSerializer> CODEC = Codec.STRING.xmap(REGISTRY::get, FluidIngredientSerializer::type);
    PacketCodec<RegistryByteBuf, FluidIngredientSerializer> PACKET_CODEC = PacketCodec.of(
        (serializer, buf) -> buf.writeString(serializer.type()),
        buf -> REGISTRY.get(buf.readString())
    );
    FluidIngredientSerializer FLUID_STACK = register("fluid_stack", FluidStackIngredient.Serializer::new);
    FluidIngredientSerializer FLUID_TAG = register("fluid_tag", FluidTagIngredient.Serializer::new);

    String type();

    MapCodec<? extends FluidIngredient> codec();

    PacketCodec<RegistryByteBuf, ? extends FluidIngredient> packetCodec();

    static FluidIngredientSerializer register(String type, Function<String, FluidIngredientSerializer> factory) {
        FluidIngredientSerializer serializer = factory.apply(type);
        REGISTRY.put(type, serializer);
        return serializer;
    }
}
