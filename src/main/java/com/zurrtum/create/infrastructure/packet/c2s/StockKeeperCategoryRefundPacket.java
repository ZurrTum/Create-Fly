package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public record StockKeeperCategoryRefundPacket(BlockPos pos, ItemStack filter) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, StockKeeperCategoryRefundPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        StockKeeperCategoryRefundPacket::pos,
        ItemStack.STREAM_CODEC,
        StockKeeperCategoryRefundPacket::filter,
        StockKeeperCategoryRefundPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<StockKeeperCategoryRefundPacket> type() {
        return AllPackets.REFUND_STOCK_KEEPER_CATEGORY;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, StockKeeperCategoryRefundPacket> callback() {
        return AllHandle::onStockKeeperCategoryRefund;
    }
}
