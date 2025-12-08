package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record ContraptionColliderLockPacket(int contraption, double offset, int sender) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, ContraptionColliderLockPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ContraptionColliderLockPacket::contraption,
        ByteBufCodecs.DOUBLE,
        ContraptionColliderLockPacket::offset,
        ByteBufCodecs.VAR_INT,
        ContraptionColliderLockPacket::sender,
        ContraptionColliderLockPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onContraptionColliderLock(this);
    }

    @Override
    public PacketType<ContraptionColliderLockPacket> type() {
        return AllPackets.CONTRAPTION_COLLIDER_LOCK;
    }
}
