package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public record ChainConveyorConnectionPacket(BlockPos pos, BlockPos targetPos, ItemStack chain, boolean connect) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ChainConveyorConnectionPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ChainConveyorConnectionPacket::pos,
        BlockPos.STREAM_CODEC,
        ChainConveyorConnectionPacket::targetPos,
        ItemStack.STREAM_CODEC,
        ChainConveyorConnectionPacket::chain,
        ByteBufCodecs.BOOL,
        ChainConveyorConnectionPacket::connect,
        ChainConveyorConnectionPacket::new
    );

    @Override
    public PacketType<ChainConveyorConnectionPacket> type() {
        return AllPackets.CHAIN_CONVEYOR_CONNECT;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ChainConveyorConnectionPacket> callback() {
        return AllHandle::onChainConveyorConnection;
    }
}
