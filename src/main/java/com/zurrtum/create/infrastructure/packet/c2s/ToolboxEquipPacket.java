package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record ToolboxEquipPacket(BlockPos toolboxPos, int slot, int hotbarSlot) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ToolboxEquipPacket> CODEC = PacketCodec.tuple(
        CatnipStreamCodecBuilders.nullable(BlockPos.PACKET_CODEC),
        ToolboxEquipPacket::toolboxPos,
        PacketCodecs.VAR_INT,
        ToolboxEquipPacket::slot,
        PacketCodecs.VAR_INT,
        ToolboxEquipPacket::hotbarSlot,
        ToolboxEquipPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onToolboxEquip((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<ToolboxEquipPacket> getPacketType() {
        return AllPackets.TOOLBOX_EQUIP;
    }
}
