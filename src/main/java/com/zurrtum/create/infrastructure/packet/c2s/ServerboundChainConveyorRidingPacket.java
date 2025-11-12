package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ServerboundChainConveyorRidingPacket(BlockPos pos, boolean stop) implements C2SPacket {
    public static final StreamCodec<ByteBuf, ServerboundChainConveyorRidingPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ServerboundChainConveyorRidingPacket::pos,
        ByteBufCodecs.BOOL,
        ServerboundChainConveyorRidingPacket::stop,
        ServerboundChainConveyorRidingPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ServerboundChainConveyorRidingPacket> type() {
        return AllPackets.CHAIN_CONVEYOR_RIDING;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ServerboundChainConveyorRidingPacket> callback() {
        return AllHandle::onServerboundChainConveyorRiding;
    }
}
