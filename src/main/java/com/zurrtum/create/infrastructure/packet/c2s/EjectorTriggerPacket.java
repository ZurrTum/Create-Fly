package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record EjectorTriggerPacket(BlockPos pos) implements C2SPacket {
    public static final StreamCodec<ByteBuf, EjectorTriggerPacket> CODEC = BlockPos.STREAM_CODEC.map(
        EjectorTriggerPacket::new,
        EjectorTriggerPacket::pos
    );

    @Override
    public PacketType<EjectorTriggerPacket> type() {
        return AllPackets.TRIGGER_EJECTOR;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, EjectorTriggerPacket> callback() {
        return AllHandle::onEjectorTrigger;
    }
}
