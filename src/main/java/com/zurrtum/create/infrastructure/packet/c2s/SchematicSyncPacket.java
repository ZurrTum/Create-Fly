package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public record SchematicSyncPacket(
    int slot, boolean deployed, BlockPos anchor, Rotation rotation, Mirror mirror
) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, SchematicSyncPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        SchematicSyncPacket::slot,
        ByteBufCodecs.BOOL,
        SchematicSyncPacket::deployed,
        BlockPos.STREAM_CODEC,
        SchematicSyncPacket::anchor,
        Rotation.STREAM_CODEC,
        SchematicSyncPacket::rotation,
        CatnipStreamCodecs.MIRROR,
        SchematicSyncPacket::mirror,
        SchematicSyncPacket::new
    );

    public SchematicSyncPacket(int slot, StructurePlaceSettings settings, BlockPos anchor, boolean deployed) {
        this(slot, deployed, anchor, settings.getRotation(), settings.getMirror());
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onSchematicSync((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<SchematicSyncPacket> type() {
        return AllPackets.SYNC_SCHEMATIC;
    }
}
