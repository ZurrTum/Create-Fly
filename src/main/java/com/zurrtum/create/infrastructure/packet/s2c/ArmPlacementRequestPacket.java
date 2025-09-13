package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record ArmPlacementRequestPacket(BlockPos pos) implements S2CPacket {
    public static final PacketCodec<ByteBuf, ArmPlacementRequestPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        ArmPlacementRequestPacket::new,
        ArmPlacementRequestPacket::pos
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ArmPlacementRequestPacket> callback() {
        return AllClientHandle::onArmPlacementRequest;
    }

    @Override
    public PacketType<ArmPlacementRequestPacket> getPacketType() {
        return AllPackets.S_PLACE_ARM;
    }
}
