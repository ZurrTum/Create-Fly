package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record RadialWrenchMenuSubmitPacket(BlockPos blockPos, BlockState newState) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, RadialWrenchMenuSubmitPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        RadialWrenchMenuSubmitPacket::blockPos,
        CatnipStreamCodecs.BLOCK_STATE,
        RadialWrenchMenuSubmitPacket::newState,
        RadialWrenchMenuSubmitPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onRadialWrenchMenuSubmit((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<RadialWrenchMenuSubmitPacket> getPacketType() {
        return AllPackets.RADIAL_WRENCH_MENU_SUBMIT;
    }
}
