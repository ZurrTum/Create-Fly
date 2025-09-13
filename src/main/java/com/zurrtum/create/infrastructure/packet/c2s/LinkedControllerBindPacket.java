package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record LinkedControllerBindPacket(int button, BlockPos linkLocation) implements C2SPacket {
    public static final PacketCodec<ByteBuf, LinkedControllerBindPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        LinkedControllerBindPacket::button,
        BlockPos.PACKET_CODEC,
        LinkedControllerBindPacket::linkLocation,
        LinkedControllerBindPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<LinkedControllerBindPacket> getPacketType() {
        return AllPackets.LINKED_CONTROLLER_BIND;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, LinkedControllerBindPacket> callback() {
        return AllHandle::onLinkedControllerBind;
    }
}
