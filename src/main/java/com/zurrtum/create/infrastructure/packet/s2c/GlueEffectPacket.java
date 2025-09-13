package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.util.TriConsumer;

public record GlueEffectPacket(BlockPos pos, Direction direction, boolean fullBlock) implements S2CPacket {
    public static final PacketCodec<ByteBuf, GlueEffectPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        GlueEffectPacket::pos,
        Direction.PACKET_CODEC,
        GlueEffectPacket::direction,
        PacketCodecs.BOOLEAN,
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
    public PacketType<GlueEffectPacket> getPacketType() {
        return AllPackets.GLUE_EFFECT;
    }
}
