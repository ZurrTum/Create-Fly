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

import java.util.List;

public record StockKeeperCategoryEditPacket(BlockPos pos, List<ItemStack> schedule) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, StockKeeperCategoryEditPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        StockKeeperCategoryEditPacket::pos,
        ItemStack.OPTIONAL_LIST_PACKET_CODEC,
        StockKeeperCategoryEditPacket::schedule,
        StockKeeperCategoryEditPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onStockKeeperCategoryEdit((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<StockKeeperCategoryEditPacket> getPacketType() {
        return AllPackets.CONFIGURE_STOCK_KEEPER_CATEGORIES;
    }
}
