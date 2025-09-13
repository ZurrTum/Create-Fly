package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record ChainConveyorConnectionPacket(BlockPos pos, BlockPos targetPos, ItemStack chain, boolean connect) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, ChainConveyorConnectionPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        ChainConveyorConnectionPacket::pos,
        BlockPos.PACKET_CODEC,
        ChainConveyorConnectionPacket::targetPos,
        ItemStack.PACKET_CODEC,
        ChainConveyorConnectionPacket::chain,
        PacketCodecs.BOOLEAN,
        ChainConveyorConnectionPacket::connect,
        ChainConveyorConnectionPacket::new
    );

    @Override
    public PacketType<ChainConveyorConnectionPacket> getPacketType() {
        return AllPackets.CHAIN_CONVEYOR_CONNECT;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ChainConveyorConnectionPacket> callback() {
        return AllHandle::onChainConveyorConnection;
    }
}
