package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record InstantSchematicPacket(String name, BlockPos origin, BlockPos bounds) implements Packet<ServerGamePacketListener> {
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
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onInstantSchematic((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<InstantSchematicPacket> type() {
        return AllPackets.INSTANT_SCHEMATIC;
    }
}
