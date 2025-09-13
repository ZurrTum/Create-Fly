package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record ToolboxEquipPacket(BlockPos toolboxPos, int slot, int hotbarSlot) implements C2SPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ToolboxEquipPacket> getPacketType() {
        return AllPackets.TOOLBOX_EQUIP;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ToolboxEquipPacket> callback() {
        return AllHandle::onToolboxEquip;
    }
}
