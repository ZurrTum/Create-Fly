package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ContraptionColliderLockPacketRequest(int contraption, double offset) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, ContraptionColliderLockPacketRequest> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ContraptionColliderLockPacketRequest::contraption,
        ByteBufCodecs.DOUBLE,
        ContraptionColliderLockPacketRequest::offset,
        ContraptionColliderLockPacketRequest::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onContraptionColliderLockRequest((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ContraptionColliderLockPacketRequest> type() {
        return AllPackets.CONTRAPTION_COLLIDER_LOCK_REQUEST;
    }
}
