package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record RemoveBlockEntityPacket(BlockPos pos) implements S2CPacket {
    public static final PacketCodec<ByteBuf, RemoveBlockEntityPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        RemoveBlockEntityPacket::new,
        RemoveBlockEntityPacket::pos
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, RemoveBlockEntityPacket> callback() {
        return AllClientHandle::onRemoveBlockEntity;
    }

    @Override
    public PacketType<RemoveBlockEntityPacket> getPacketType() {
        return AllPackets.REMOVE_TE;
    }
}
