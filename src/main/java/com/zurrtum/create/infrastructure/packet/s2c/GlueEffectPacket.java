package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record GlueEffectPacket(BlockPos pos, Direction direction, boolean fullBlock) implements S2CPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, GlueEffectPacket> callback() {
        return AllClientHandle::onGlueEffect;
    }

    @Override
    public PacketType<GlueEffectPacket> type() {
        return AllPackets.GLUE_EFFECT;
    }
}
