package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.BiConsumer;

public record StockKeeperCategoryEditPacket(BlockPos pos, List<ItemStack> schedule) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, StockKeeperCategoryEditPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        StockKeeperCategoryEditPacket::pos,
        ItemStack.OPTIONAL_LIST_PACKET_CODEC,
        StockKeeperCategoryEditPacket::schedule,
        StockKeeperCategoryEditPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<StockKeeperCategoryEditPacket> getPacketType() {
        return AllPackets.CONFIGURE_STOCK_KEEPER_CATEGORIES;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, StockKeeperCategoryEditPacket> callback() {
        return AllHandle::onStockKeeperCategoryEdit;
    }
}
