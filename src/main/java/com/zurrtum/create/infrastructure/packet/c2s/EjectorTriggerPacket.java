package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record EjectorTriggerPacket(BlockPos pos) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, EjectorTriggerPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        EjectorTriggerPacket::new,
        EjectorTriggerPacket::pos
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onEjectorTrigger((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<EjectorTriggerPacket> getPacketType() {
        return AllPackets.TRIGGER_EJECTOR;
    }
}
