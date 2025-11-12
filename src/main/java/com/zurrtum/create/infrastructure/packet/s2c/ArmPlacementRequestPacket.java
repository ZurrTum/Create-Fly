package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record ArmPlacementRequestPacket(BlockPos pos) implements S2CPacket {
    public static final StreamCodec<ByteBuf, ArmPlacementRequestPacket> CODEC = BlockPos.STREAM_CODEC.map(
        ArmPlacementRequestPacket::new,
        ArmPlacementRequestPacket::pos
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ArmPlacementRequestPacket> callback() {
        return AllClientHandle::onArmPlacementRequest;
    }

    @Override
    public PacketType<ArmPlacementRequestPacket> type() {
        return AllPackets.S_PLACE_ARM;
    }
}
