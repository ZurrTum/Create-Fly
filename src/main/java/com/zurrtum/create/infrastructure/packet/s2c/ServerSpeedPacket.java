package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record ServerSpeedPacket(int speed) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ServerSpeedPacket> CODEC = PacketCodecs.INTEGER.xmap(ServerSpeedPacket::new, ServerSpeedPacket::speed);

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onServerSpeed(this);
    }

    @Override
    public PacketType<ServerSpeedPacket> getPacketType() {
        return AllPackets.SERVER_SPEED;
    }
}
