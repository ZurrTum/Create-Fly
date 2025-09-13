package com.zurrtum.create.content.decoration.slidingDoor;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

import java.util.Locale;

public enum DoorControl implements StringIdentifiable {

    ALL,
    NORTH,
    EAST,
    SOUTH,
    WEST,
    NONE;

    public static final Codec<DoorControl> CODEC = StringIdentifiable.createCodec(DoorControl::values);
    public static final PacketCodec<ByteBuf, DoorControl> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(DoorControl.class);

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean matches(Direction doorDirection) {
        return switch (this) {
            case ALL -> true;
            case NORTH -> doorDirection == Direction.NORTH;
            case EAST -> doorDirection == Direction.EAST;
            case SOUTH -> doorDirection == Direction.SOUTH;
            case WEST -> doorDirection == Direction.WEST;
            default -> false;
        };
    }
}
