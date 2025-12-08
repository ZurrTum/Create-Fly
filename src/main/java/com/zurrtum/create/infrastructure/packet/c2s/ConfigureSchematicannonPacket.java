package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ConfigureSchematicannonPacket(Option option, boolean set) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureSchematicannonPacket> CODEC = StreamCodec.composite(
        Option.CODEC,
        ConfigureSchematicannonPacket::option,
        ByteBufCodecs.BOOL,
        ConfigureSchematicannonPacket::set,
        ConfigureSchematicannonPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onConfigureSchematicannon((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ConfigureSchematicannonPacket> type() {
        return AllPackets.CONFIGURE_SCHEMATICANNON;
    }

    public enum Option {
        DONT_REPLACE,
        REPLACE_SOLID,
        REPLACE_ANY,
        REPLACE_EMPTY,
        SKIP_MISSING,
        SKIP_BLOCK_ENTITIES,
        PLAY,
        PAUSE,
        STOP;

        public static final StreamCodec<ByteBuf, Option> CODEC = CatnipStreamCodecBuilders.ofEnum(Option.class);
    }
}
