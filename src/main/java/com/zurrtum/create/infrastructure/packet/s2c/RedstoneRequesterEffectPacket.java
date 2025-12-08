package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record RedstoneRequesterEffectPacket(BlockPos pos, boolean success) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, RedstoneRequesterEffectPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        RedstoneRequesterEffectPacket::pos,
        ByteBufCodecs.BOOL,
        RedstoneRequesterEffectPacket::success,
        RedstoneRequesterEffectPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onRedstoneRequesterEffect(listener, this);
    }

    @Override
    public PacketType<RedstoneRequesterEffectPacket> type() {
        return AllPackets.REDSTONE_REQUESTER_EFFECT;
    }
}
