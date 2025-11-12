package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record EjectorPlacementPacket(int h, int v, BlockPos pos, Direction facing) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, EjectorPlacementPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        EjectorPlacementPacket::h,
        ByteBufCodecs.INT,
        EjectorPlacementPacket::v,
        BlockPos.STREAM_CODEC,
        EjectorPlacementPacket::pos,
        Direction.STREAM_CODEC,
        EjectorPlacementPacket::facing,
        EjectorPlacementPacket::new
    );

    @Override
    public PacketType<EjectorPlacementPacket> type() {
        return AllPackets.PLACE_EJECTOR;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, EjectorPlacementPacket> callback() {
        return AllHandle::onEjectorPlacement;
    }
}
