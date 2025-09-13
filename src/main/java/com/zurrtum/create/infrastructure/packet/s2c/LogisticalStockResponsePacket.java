package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.BigItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;

public record LogisticalStockResponsePacket(boolean lastPacket, BlockPos pos, List<BigItemStack> items) implements S2CPacket {
    public static final PacketCodec<RegistryByteBuf, LogisticalStockResponsePacket> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN,
        LogisticalStockResponsePacket::lastPacket,
        BlockPos.PACKET_CODEC,
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
    public PacketType<LogisticalStockResponsePacket> getPacketType() {
        return AllPackets.LOGISTICS_STOCK_RESPONSE;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, LogisticalStockResponsePacket> callback() {
        return AllClientHandle::onLogisticalStockResponse;
    }
}
