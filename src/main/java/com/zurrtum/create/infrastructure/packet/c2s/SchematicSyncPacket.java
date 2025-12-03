package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

public record SchematicSyncPacket(
    int slot, boolean deployed, BlockPos anchor, BlockRotation rotation, BlockMirror mirror
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, SchematicSyncPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        SchematicSyncPacket::slot,
        PacketCodecs.BOOLEAN,
        SchematicSyncPacket::deployed,
        BlockPos.PACKET_CODEC,
        SchematicSyncPacket::anchor,
        BlockRotation.PACKET_CODEC,
        SchematicSyncPacket::rotation,
        CatnipStreamCodecs.MIRROR,
        SchematicSyncPacket::mirror,
        SchematicSyncPacket::new
    );

    public SchematicSyncPacket(int slot, StructurePlacementData settings, BlockPos anchor, boolean deployed) {
        this(slot, deployed, anchor, settings.getRotation(), settings.getMirror());
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onSchematicSync((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<SchematicSyncPacket> getPacketType() {
        return AllPackets.SYNC_SCHEMATIC;
    }
}
