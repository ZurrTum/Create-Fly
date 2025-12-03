package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record ServerboundChainConveyorRidingPacket(BlockPos pos, boolean stop) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ServerboundChainConveyorRidingPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        ServerboundChainConveyorRidingPacket::pos,
        PacketCodecs.BOOLEAN,
        ServerboundChainConveyorRidingPacket::stop,
        ServerboundChainConveyorRidingPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onServerboundChainConveyorRiding((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<ServerboundChainConveyorRidingPacket> getPacketType() {
        return AllPackets.CHAIN_CONVEYOR_RIDING;
    }
}
