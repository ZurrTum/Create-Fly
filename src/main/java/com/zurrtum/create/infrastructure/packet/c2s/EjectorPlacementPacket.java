package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record EjectorPlacementPacket(int h, int v, BlockPos pos, Direction facing) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, EjectorPlacementPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        EjectorPlacementPacket::h,
        PacketCodecs.INTEGER,
        EjectorPlacementPacket::v,
        BlockPos.PACKET_CODEC,
        EjectorPlacementPacket::pos,
        Direction.PACKET_CODEC,
        EjectorPlacementPacket::facing,
        EjectorPlacementPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onEjectorPlacement((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<EjectorPlacementPacket> getPacketType() {
        return AllPackets.PLACE_EJECTOR;
    }
}
