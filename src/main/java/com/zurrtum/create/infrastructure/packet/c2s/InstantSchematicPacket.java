package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record InstantSchematicPacket(String name, BlockPos origin, BlockPos bounds) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, InstantSchematicPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING,
        InstantSchematicPacket::name,
        BlockPos.PACKET_CODEC,
        InstantSchematicPacket::origin,
        BlockPos.PACKET_CODEC,
        InstantSchematicPacket::bounds,
        InstantSchematicPacket::new
    );

    @Override
    public PacketType<InstantSchematicPacket> getPacketType() {
        return AllPackets.INSTANT_SCHEMATIC;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, InstantSchematicPacket> callback() {
        return AllHandle::onInstantSchematic;
    }
}
