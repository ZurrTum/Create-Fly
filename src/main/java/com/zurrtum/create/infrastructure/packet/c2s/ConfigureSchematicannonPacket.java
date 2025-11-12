package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ConfigureSchematicannonPacket(Option option, boolean set) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureSchematicannonPacket> CODEC = StreamCodec.composite(
        Option.CODEC,
        ConfigureSchematicannonPacket::option,
        ByteBufCodecs.BOOL,
        ConfigureSchematicannonPacket::set,
        ConfigureSchematicannonPacket::new
    );

    @Override
    public PacketType<ConfigureSchematicannonPacket> type() {
        return AllPackets.CONFIGURE_SCHEMATICANNON;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ConfigureSchematicannonPacket> callback() {
        return AllHandle::onConfigureSchematicannon;
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
