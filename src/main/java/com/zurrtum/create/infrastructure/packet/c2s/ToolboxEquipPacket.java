package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.Nullable;

public record ToolboxEquipPacket(@Nullable BlockPos toolboxPos, int slot, int hotbarSlot) implements Packet<ServerGamePacketListener> {
    @SuppressWarnings("DataFlowIssue")
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
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onToolboxEquip((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ToolboxEquipPacket> type() {
        return AllPackets.TOOLBOX_EQUIP;
    }
}
