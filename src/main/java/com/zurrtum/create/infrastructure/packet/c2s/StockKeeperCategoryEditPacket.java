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

import java.util.List;

public record StockKeeperCategoryEditPacket(BlockPos pos, List<ItemStack> schedule) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, StockKeeperCategoryEditPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        StockKeeperCategoryEditPacket::pos,
        ItemStack.OPTIONAL_LIST_STREAM_CODEC,
        StockKeeperCategoryEditPacket::schedule,
        StockKeeperCategoryEditPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onStockKeeperCategoryEdit((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<StockKeeperCategoryEditPacket> type() {
        return AllPackets.CONFIGURE_STOCK_KEEPER_CATEGORIES;
    }
}
