package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record ServerSpeedPacket(int speed) implements S2CPacket {
    public static final PacketCodec<ByteBuf, ServerSpeedPacket> CODEC = PacketCodecs.INTEGER.xmap(ServerSpeedPacket::new, ServerSpeedPacket::speed);

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ServerSpeedPacket> callback() {
        return AllClientHandle::onServerSpeed;
    }

    @Override
    public PacketType<ServerSpeedPacket> getPacketType() {
        return AllPackets.SERVER_SPEED;
    }
}
