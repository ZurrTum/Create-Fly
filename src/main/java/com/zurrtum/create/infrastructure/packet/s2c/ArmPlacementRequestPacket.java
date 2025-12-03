package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

public record ArmPlacementRequestPacket(BlockPos pos) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ArmPlacementRequestPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        ArmPlacementRequestPacket::new,
        ArmPlacementRequestPacket::pos
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onArmPlacementRequest(this);
    }

    @Override
    public PacketType<ArmPlacementRequestPacket> getPacketType() {
        return AllPackets.S_PLACE_ARM;
    }
}
