package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record FunnelFlapPacket(BlockPos pos, boolean inwards) implements S2CPacket {
    public static final PacketCodec<ByteBuf, FunnelFlapPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        FunnelFlapPacket::pos,
        PacketCodecs.BOOLEAN,
        FunnelFlapPacket::inwards,
        FunnelFlapPacket::new
    );

    public FunnelFlapPacket(FunnelBlockEntity blockEntity, boolean inwards) {
        this(blockEntity.getPos(), inwards);
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, FunnelFlapPacket> callback() {
        return AllClientHandle::onFunnelFlap;
    }

    @Override
    public PacketType<FunnelFlapPacket> getPacketType() {
        return AllPackets.FUNNEL_FLAP;
    }
}
