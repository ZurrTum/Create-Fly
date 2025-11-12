package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.BigItemStack;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record LogisticalStockResponsePacket(boolean lastPacket, BlockPos pos, List<BigItemStack> items) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, LogisticalStockResponsePacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        LogisticalStockResponsePacket::lastPacket,
        BlockPos.STREAM_CODEC,
        LogisticalStockResponsePacket::pos,
        CatnipStreamCodecBuilders.list(BigItemStack.STREAM_CODEC),
        LogisticalStockResponsePacket::items,
        LogisticalStockResponsePacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<LogisticalStockResponsePacket> type() {
        return AllPackets.LOGISTICS_STOCK_RESPONSE;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, LogisticalStockResponsePacket> callback() {
        return AllClientHandle::onLogisticalStockResponse;
    }
}
