package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record GlueEffectPacket(BlockPos pos, Direction direction, boolean fullBlock) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, GlueEffectPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        GlueEffectPacket::pos,
        Direction.STREAM_CODEC,
        GlueEffectPacket::direction,
        ByteBufCodecs.BOOL,
        GlueEffectPacket::fullBlock,
        GlueEffectPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onGlueEffect(listener, this);
    }

    @Override
    public PacketType<GlueEffectPacket> type() {
        return AllPackets.GLUE_EFFECT;
    }
}
