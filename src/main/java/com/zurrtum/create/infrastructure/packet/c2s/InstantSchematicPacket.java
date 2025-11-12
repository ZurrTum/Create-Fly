package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record InstantSchematicPacket(String name, BlockPos origin, BlockPos bounds) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, InstantSchematicPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        InstantSchematicPacket::name,
        BlockPos.STREAM_CODEC,
        InstantSchematicPacket::origin,
        BlockPos.STREAM_CODEC,
        InstantSchematicPacket::bounds,
        InstantSchematicPacket::new
    );

    @Override
    public PacketType<InstantSchematicPacket> type() {
        return AllPackets.INSTANT_SCHEMATIC;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, InstantSchematicPacket> callback() {
        return AllHandle::onInstantSchematic;
    }
}
