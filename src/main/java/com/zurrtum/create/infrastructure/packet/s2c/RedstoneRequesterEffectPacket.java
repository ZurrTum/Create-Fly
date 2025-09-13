package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record RedstoneRequesterEffectPacket(BlockPos pos, boolean success) implements S2CPacket {
    public static final PacketCodec<ByteBuf, RedstoneRequesterEffectPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        RedstoneRequesterEffectPacket::pos,
        PacketCodecs.BOOLEAN,
        RedstoneRequesterEffectPacket::success,
        RedstoneRequesterEffectPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, RedstoneRequesterEffectPacket> callback() {
        return AllClientHandle::onRedstoneRequesterEffect;
    }

    @Override
    public PacketType<RedstoneRequesterEffectPacket> getPacketType() {
        return AllPackets.REDSTONE_REQUESTER_EFFECT;
    }
}
