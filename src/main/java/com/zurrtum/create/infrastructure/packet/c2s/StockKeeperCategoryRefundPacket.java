package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record StockKeeperCategoryRefundPacket(BlockPos pos, ItemStack filter) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, StockKeeperCategoryRefundPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        StockKeeperCategoryRefundPacket::pos,
        ItemStack.PACKET_CODEC,
        StockKeeperCategoryRefundPacket::filter,
        StockKeeperCategoryRefundPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<StockKeeperCategoryRefundPacket> getPacketType() {
        return AllPackets.REFUND_STOCK_KEEPER_CATEGORY;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, StockKeeperCategoryRefundPacket> callback() {
        return AllHandle::onStockKeeperCategoryRefund;
    }
}
