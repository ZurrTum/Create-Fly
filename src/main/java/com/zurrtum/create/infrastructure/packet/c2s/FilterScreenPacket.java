package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public record FilterScreenPacket(Option option, @Nullable NbtCompound data) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, FilterScreenPacket> CODEC = PacketCodec.tuple(
        Option.STREAM_CODEC,
        FilterScreenPacket::option,
        CatnipStreamCodecBuilders.nullable(PacketCodecs.NBT_COMPOUND),
        FilterScreenPacket::data,
        FilterScreenPacket::new
    );

    public FilterScreenPacket(Option option) {
        this(option, null);
    }

    @Override
    public PacketType<FilterScreenPacket> getPacketType() {
        return AllPackets.CONFIGURE_FILTER;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, FilterScreenPacket> callback() {
        return AllHandle::onFilterScreen;
    }

    public enum Option {
        WHITELIST,
        WHITELIST2,
        BLACKLIST,
        RESPECT_DATA,
        IGNORE_DATA,
        UPDATE_FILTER_ITEM,
        ADD_TAG,
        ADD_INVERTED_TAG,
        UPDATE_ADDRESS;

        public static final PacketCodec<ByteBuf, Option> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(Option.class);
    }
}
