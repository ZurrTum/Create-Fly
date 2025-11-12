package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public record SchematicSyncPacket(int slot, boolean deployed, BlockPos anchor, Rotation rotation, Mirror mirror) implements C2SPacket {
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
    public PacketType<SchematicSyncPacket> type() {
        return AllPackets.SYNC_SCHEMATIC;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, SchematicSyncPacket> callback() {
        return AllHandle::onSchematicSync;
    }
}
