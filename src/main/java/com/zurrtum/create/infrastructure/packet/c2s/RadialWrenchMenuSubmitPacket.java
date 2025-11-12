package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.state.BlockState;

public record RadialWrenchMenuSubmitPacket(BlockPos blockPos, BlockState newState) implements C2SPacket {
    public static final StreamCodec<ByteBuf, RadialWrenchMenuSubmitPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        RadialWrenchMenuSubmitPacket::blockPos,
        CatnipStreamCodecs.BLOCK_STATE,
        RadialWrenchMenuSubmitPacket::newState,
        RadialWrenchMenuSubmitPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<RadialWrenchMenuSubmitPacket> type() {
        return AllPackets.RADIAL_WRENCH_MENU_SUBMIT;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, RadialWrenchMenuSubmitPacket> callback() {
        return AllHandle::onRadialWrenchMenuSubmit;
    }
}
