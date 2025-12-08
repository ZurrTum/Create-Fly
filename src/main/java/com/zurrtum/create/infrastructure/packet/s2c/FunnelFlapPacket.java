package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record FunnelFlapPacket(BlockPos pos, boolean inwards) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, FunnelFlapPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        FunnelFlapPacket::pos,
        ByteBufCodecs.BOOL,
        FunnelFlapPacket::inwards,
        FunnelFlapPacket::new
    );

    public FunnelFlapPacket(FunnelBlockEntity blockEntity, boolean inwards) {
        this(blockEntity.getBlockPos(), inwards);
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onFunnelFlap(listener, this);
    }

    @Override
    public PacketType<FunnelFlapPacket> type() {
        return AllPackets.FUNNEL_FLAP;
    }
}
