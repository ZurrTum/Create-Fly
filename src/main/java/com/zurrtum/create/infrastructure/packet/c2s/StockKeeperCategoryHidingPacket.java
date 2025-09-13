package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.BiConsumer;

public record StockKeeperCategoryHidingPacket(BlockPos pos, List<Integer> indices) implements C2SPacket {
    public static final PacketCodec<ByteBuf, StockKeeperCategoryHidingPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        StockKeeperCategoryHidingPacket::pos,
        CatnipStreamCodecBuilders.list(PacketCodecs.INTEGER),
        StockKeeperCategoryHidingPacket::indices,
        StockKeeperCategoryHidingPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<StockKeeperCategoryHidingPacket> getPacketType() {
        return AllPackets.STOCK_KEEPER_HIDE_CATEGORY;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, StockKeeperCategoryHidingPacket> callback() {
        return AllHandle::onStockKeeperCategoryHiding;
    }
}
