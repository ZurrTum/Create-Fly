package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;

import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public record StockKeeperCategoryEditPacket(BlockPos pos, List<ItemStack> schedule) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, StockKeeperCategoryEditPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        StockKeeperCategoryEditPacket::pos,
        ItemStack.OPTIONAL_LIST_STREAM_CODEC,
        StockKeeperCategoryEditPacket::schedule,
        StockKeeperCategoryEditPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<StockKeeperCategoryEditPacket> type() {
        return AllPackets.CONFIGURE_STOCK_KEEPER_CATEGORIES;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, StockKeeperCategoryEditPacket> callback() {
        return AllHandle::onStockKeeperCategoryEdit;
    }
}
