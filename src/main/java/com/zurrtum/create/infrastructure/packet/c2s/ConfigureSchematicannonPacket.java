package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record ConfigureSchematicannonPacket(Option option, boolean set) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, ConfigureSchematicannonPacket> CODEC = PacketCodec.tuple(
        Option.CODEC,
        ConfigureSchematicannonPacket::option,
        PacketCodecs.BOOLEAN,
        ConfigureSchematicannonPacket::set,
        ConfigureSchematicannonPacket::new
    );

    @Override
    public PacketType<ConfigureSchematicannonPacket> getPacketType() {
        return AllPackets.CONFIGURE_SCHEMATICANNON;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ConfigureSchematicannonPacket> callback() {
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

        public static final PacketCodec<ByteBuf, Option> CODEC = CatnipStreamCodecBuilders.ofEnum(Option.class);
    }
}
