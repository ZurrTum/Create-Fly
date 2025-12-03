package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record StockKeeperCategoryRefundPacket(BlockPos pos, ItemStack filter) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, StockKeeperCategoryRefundPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        StockKeeperCategoryRefundPacket::pos,
        ItemStack.PACKET_CODEC,
        StockKeeperCategoryRefundPacket::filter,
        StockKeeperCategoryRefundPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onStockKeeperCategoryRefund((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<StockKeeperCategoryRefundPacket> getPacketType() {
        return AllPackets.REFUND_STOCK_KEEPER_CATEGORY;
    }
}
