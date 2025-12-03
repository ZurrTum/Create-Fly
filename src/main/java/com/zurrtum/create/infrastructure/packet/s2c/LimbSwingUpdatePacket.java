package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.Vec3d;

public record LimbSwingUpdatePacket(int entityId, Vec3d position, float limbSwing) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, LimbSwingUpdatePacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        LimbSwingUpdatePacket::entityId,
        Vec3d.PACKET_CODEC,
        LimbSwingUpdatePacket::position,
        PacketCodecs.FLOAT,
        LimbSwingUpdatePacket::limbSwing,
        LimbSwingUpdatePacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onLimbSwingUpdate(listener, this);
    }

    @Override
    public PacketType<LimbSwingUpdatePacket> getPacketType() {
        return AllPackets.LIMBSWING_UPDATE;
    }
}
