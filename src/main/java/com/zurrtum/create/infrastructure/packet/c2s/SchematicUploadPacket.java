package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public record SchematicUploadPacket(int code, long size, String schematic, byte[] data) implements Packet<ServerPlayPacketListener> {
    public static final int BEGIN = 0;
    public static final int WRITE = 1;
    public static final int FINISH = 2;

    public static final PacketCodec<RegistryByteBuf, SchematicUploadPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        SchematicUploadPacket::code,
        PacketCodecs.VAR_LONG,
        SchematicUploadPacket::size,
        CatnipStreamCodecBuilders.nullable(PacketCodecs.string(256)),
        SchematicUploadPacket::schematic,
        CatnipStreamCodecBuilders.nullable(PacketCodecs.byteArray(Integer.MAX_VALUE)),
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
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onSchematicUpload((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<SchematicUploadPacket> getPacketType() {
        return AllPackets.UPLOAD_SCHEMATIC;
    }
}
