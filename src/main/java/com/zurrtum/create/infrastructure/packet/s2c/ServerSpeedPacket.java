package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record ServerSpeedPacket(int speed) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, ServerSpeedPacket> CODEC = ByteBufCodecs.INT.map(ServerSpeedPacket::new, ServerSpeedPacket::speed);

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onServerSpeed(this);
    }

    @Override
    public PacketType<ServerSpeedPacket> type() {
        return AllPackets.SERVER_SPEED;
    }
}
