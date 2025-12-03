package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public record ContraptionColliderLockPacketRequest(int contraption, double offset) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ContraptionColliderLockPacketRequest> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        ContraptionColliderLockPacketRequest::contraption,
        PacketCodecs.DOUBLE,
        ContraptionColliderLockPacketRequest::offset,
        ContraptionColliderLockPacketRequest::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onContraptionColliderLockRequest((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<ContraptionColliderLockPacketRequest> getPacketType() {
        return AllPackets.CONTRAPTION_COLLIDER_LOCK_REQUEST;
    }
}
