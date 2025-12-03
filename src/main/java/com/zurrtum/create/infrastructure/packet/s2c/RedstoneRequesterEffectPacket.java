package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

public record RedstoneRequesterEffectPacket(BlockPos pos, boolean success) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, RedstoneRequesterEffectPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        RedstoneRequesterEffectPacket::pos,
        PacketCodecs.BOOLEAN,
        RedstoneRequesterEffectPacket::success,
        RedstoneRequesterEffectPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onRedstoneRequesterEffect(listener, this);
    }

    @Override
    public PacketType<RedstoneRequesterEffectPacket> getPacketType() {
        return AllPackets.REDSTONE_REQUESTER_EFFECT;
    }
}
