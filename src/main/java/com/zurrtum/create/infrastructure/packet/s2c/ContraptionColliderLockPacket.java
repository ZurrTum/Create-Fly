package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record ContraptionColliderLockPacket(int contraption, double offset, int sender) implements Packet<ClientPlayPacketListener> {
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
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onContraptionColliderLock(this);
    }

    @Override
    public PacketType<ContraptionColliderLockPacket> getPacketType() {
        return AllPackets.CONTRAPTION_COLLIDER_LOCK;
    }
}
