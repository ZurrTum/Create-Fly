package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ContraptionColliderLockPacketRequest(int contraption, double offset) implements C2SPacket {
    public static final StreamCodec<ByteBuf, ContraptionColliderLockPacketRequest> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ContraptionColliderLockPacketRequest::contraption,
        ByteBufCodecs.DOUBLE,
        ContraptionColliderLockPacketRequest::offset,
        ContraptionColliderLockPacketRequest::new
    );

    @Override
    public PacketType<ContraptionColliderLockPacketRequest> type() {
        return AllPackets.CONTRAPTION_COLLIDER_LOCK_REQUEST;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ContraptionColliderLockPacketRequest> callback() {
        return AllHandle::onContraptionColliderLockRequest;
    }
}
