package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.Nullable;

public record SchematicUploadPacket(
    int code, long size, String schematic, byte @Nullable [] data
) implements Packet<ServerGamePacketListener> {
    public static final int BEGIN = 0;
    public static final int WRITE = 1;
    public static final int FINISH = 2;

    @SuppressWarnings("DataFlowIssue")
    public static final StreamCodec<RegistryFriendlyByteBuf, SchematicUploadPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        SchematicUploadPacket::code,
        ByteBufCodecs.VAR_LONG,
        SchematicUploadPacket::size,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.stringUtf8(256)),
        SchematicUploadPacket::schematic,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.byteArray(Integer.MAX_VALUE)),
        SchematicUploadPacket::data,
        SchematicUploadPacket::new
    );

    public static SchematicUploadPacket begin(String schematic, long size) {
        return new SchematicUploadPacket(BEGIN, size, schematic, null);
    }

    public static SchematicUploadPacket write(String schematic, byte[] data) {
        return new SchematicUploadPacket(WRITE, 0, schematic, data);
    }

    public static SchematicUploadPacket finish(String schematic) {
        return new SchematicUploadPacket(FINISH, 0, schematic, null);
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onSchematicUpload((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<SchematicUploadPacket> type() {
        return AllPackets.UPLOAD_SCHEMATIC;
    }
}
