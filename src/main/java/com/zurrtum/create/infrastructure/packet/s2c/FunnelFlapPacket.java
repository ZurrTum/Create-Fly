package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record FunnelFlapPacket(BlockPos pos, boolean inwards) implements S2CPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, FunnelFlapPacket> callback() {
        return AllClientHandle::onFunnelFlap;
    }

    @Override
    public PacketType<FunnelFlapPacket> type() {
        return AllPackets.FUNNEL_FLAP;
    }
}
