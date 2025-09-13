package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.util.TriConsumer;

public record LimbSwingUpdatePacket(int entityId, Vec3d position, float limbSwing) implements S2CPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<LimbSwingUpdatePacket> getPacketType() {
        return AllPackets.LIMBSWING_UPDATE;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, LimbSwingUpdatePacket> callback() {
        return AllClientHandle::onLimbSwingUpdate;
    }
}
