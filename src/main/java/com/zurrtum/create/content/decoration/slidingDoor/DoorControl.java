package com.zurrtum.create.content.decoration.slidingDoor;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum DoorControl implements StringRepresentable {

    ALL,
    NORTH,
    EAST,
    SOUTH,
    WEST,
    NONE;

    public static final Codec<DoorControl> CODEC = StringRepresentable.fromEnum(DoorControl::values);
    public static final StreamCodec<ByteBuf, DoorControl> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(DoorControl.class);

    @Override
    public String getSerializedName() {
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
