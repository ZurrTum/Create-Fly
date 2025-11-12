package com.zurrtum.create.content.trains.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.Create.MOD_ID;

public record TrainIconType(ResourceLocation id) {
    public static final Codec<TrainIconType> CODEC = ResourceLocation.CODEC.xmap(TrainIconType::byId, TrainIconType::id);
    public static final StreamCodec<ByteBuf, TrainIconType> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(TrainIconType::byId, TrainIconType::id);

    public static final Map<ResourceLocation, TrainIconType> ALL = new HashMap<>();
    public static final TrainIconType TRADITIONAL = register("traditional");
    public static final TrainIconType ELECTRIC = register("electric");
    public static final TrainIconType MODERN = register("modern");

    private static TrainIconType register(String id) {
        TrainIconType type = new TrainIconType(ResourceLocation.fromNamespaceAndPath(MOD_ID, id));
        ALL.put(type.id, type);
        return type;
    }

    public static TrainIconType byId(ResourceLocation id) {
        return ALL.getOrDefault(id, TRADITIONAL);
    }
}
