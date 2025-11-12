package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record LinkedControllerBindPacket(int button, BlockPos linkLocation) implements C2SPacket {
    public static final StreamCodec<ByteBuf, LinkedControllerBindPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        LinkedControllerBindPacket::button,
        BlockPos.STREAM_CODEC,
        LinkedControllerBindPacket::linkLocation,
        LinkedControllerBindPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<LinkedControllerBindPacket> type() {
        return AllPackets.LINKED_CONTROLLER_BIND;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, LinkedControllerBindPacket> callback() {
        return AllHandle::onLinkedControllerBind;
    }
}
