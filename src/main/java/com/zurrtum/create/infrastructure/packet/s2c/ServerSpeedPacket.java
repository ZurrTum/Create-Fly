package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record ServerSpeedPacket(int speed) implements S2CPacket {
    public static final StreamCodec<ByteBuf, ServerSpeedPacket> CODEC = ByteBufCodecs.INT.map(ServerSpeedPacket::new, ServerSpeedPacket::speed);

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ServerSpeedPacket> callback() {
        return AllClientHandle::onServerSpeed;
    }

    @Override
    public PacketType<ServerSpeedPacket> type() {
        return AllPackets.SERVER_SPEED;
    }
}
