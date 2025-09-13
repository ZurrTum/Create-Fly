package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record ContraptionColliderLockPacket(int contraption, double offset, int sender) implements S2CPacket {
    public static final PacketCodec<ByteBuf, ContraptionColliderLockPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        ContraptionColliderLockPacket::contraption,
        PacketCodecs.DOUBLE,
        ContraptionColliderLockPacket::offset,
        PacketCodecs.VAR_INT,
        ContraptionColliderLockPacket::sender,
        ContraptionColliderLockPacket::new
    );

    @Override
    public PacketType<ContraptionColliderLockPacket> getPacketType() {
        return AllPackets.CONTRAPTION_COLLIDER_LOCK;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ContraptionColliderLockPacket> callback() {
        return AllClientHandle::onContraptionColliderLock;
    }
}
