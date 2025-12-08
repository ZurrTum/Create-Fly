package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public record StockKeeperCategoryRefundPacket(BlockPos pos, ItemStack filter) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, StockKeeperCategoryRefundPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        StockKeeperCategoryRefundPacket::pos,
        ItemStack.STREAM_CODEC,
        StockKeeperCategoryRefundPacket::filter,
        StockKeeperCategoryRefundPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onStockKeeperCategoryRefund((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<StockKeeperCategoryRefundPacket> type() {
        return AllPackets.REFUND_STOCK_KEEPER_CATEGORY;
    }
}
