package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record StockKeeperCategoryHidingPacket(BlockPos pos, List<Integer> indices) implements C2SPacket {
    public static final StreamCodec<ByteBuf, StockKeeperCategoryHidingPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        StockKeeperCategoryHidingPacket::pos,
        CatnipStreamCodecBuilders.list(ByteBufCodecs.INT),
        StockKeeperCategoryHidingPacket::indices,
        StockKeeperCategoryHidingPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<StockKeeperCategoryHidingPacket> type() {
        return AllPackets.STOCK_KEEPER_HIDE_CATEGORY;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, StockKeeperCategoryHidingPacket> callback() {
        return AllHandle::onStockKeeperCategoryHiding;
    }
}
