package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ToolboxEquipPacket(BlockPos toolboxPos, int slot, int hotbarSlot) implements C2SPacket {
    public static final StreamCodec<ByteBuf, ToolboxEquipPacket> CODEC = StreamCodec.composite(
        CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC),
        ToolboxEquipPacket::toolboxPos,
        ByteBufCodecs.VAR_INT,
        ToolboxEquipPacket::slot,
        ByteBufCodecs.VAR_INT,
        ToolboxEquipPacket::hotbarSlot,
        ToolboxEquipPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ToolboxEquipPacket> type() {
        return AllPackets.TOOLBOX_EQUIP;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ToolboxEquipPacket> callback() {
        return AllHandle::onToolboxEquip;
    }
}
