package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record ContraptionColliderLockPacketRequest(int contraption, double offset) implements C2SPacket {
    public static final PacketCodec<ByteBuf, ContraptionColliderLockPacketRequest> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        ContraptionColliderLockPacketRequest::contraption,
        PacketCodecs.DOUBLE,
        ContraptionColliderLockPacketRequest::offset,
        ContraptionColliderLockPacketRequest::new
    );

    @Override
    public PacketType<ContraptionColliderLockPacketRequest> getPacketType() {
        return AllPackets.CONTRAPTION_COLLIDER_LOCK_REQUEST;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ContraptionColliderLockPacketRequest> callback() {
        return AllHandle::onContraptionColliderLockRequest;
    }
}
