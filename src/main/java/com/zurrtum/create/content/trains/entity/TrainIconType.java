package com.zurrtum.create.content.trains.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

import static com.zurrtum.create.Create.MOD_ID;

public record TrainIconType(Identifier id) {
    public static final Codec<TrainIconType> CODEC = Identifier.CODEC.xmap(TrainIconType::byId, TrainIconType::id);
    public static final PacketCodec<ByteBuf, TrainIconType> STREAM_CODEC = Identifier.PACKET_CODEC.xmap(TrainIconType::byId, TrainIconType::id);

    public static final Map<Identifier, TrainIconType> ALL = new HashMap<>();
    public static final TrainIconType TRADITIONAL = register("traditional");
    public static final TrainIconType ELECTRIC = register("electric");
    public static final TrainIconType MODERN = register("modern");

    private static TrainIconType register(String id) {
        TrainIconType type = new TrainIconType(Identifier.of(MOD_ID, id));
        ALL.put(type.id, type);
        return type;
    }

    public static TrainIconType byId(Identifier id) {
        return ALL.getOrDefault(id, TRADITIONAL);
    }
}
