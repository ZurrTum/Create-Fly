package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record FilterScreenPacket(Option option, @Nullable CompoundTag data) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, FilterScreenPacket> CODEC = StreamCodec.composite(
        Option.STREAM_CODEC,
        FilterScreenPacket::option,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.COMPOUND_TAG),
        FilterScreenPacket::data,
        FilterScreenPacket::new
    );

    public FilterScreenPacket(Option option) {
        this(option, null);
    }

    @Override
    public PacketType<FilterScreenPacket> type() {
        return AllPackets.CONFIGURE_FILTER;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, FilterScreenPacket> callback() {
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

        public static final StreamCodec<ByteBuf, Option> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(Option.class);
    }
}
