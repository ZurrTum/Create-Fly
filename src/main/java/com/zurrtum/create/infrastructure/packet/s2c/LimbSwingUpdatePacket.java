package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

public record LimbSwingUpdatePacket(int entityId, Vec3 position, float limbSwing) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, LimbSwingUpdatePacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        LimbSwingUpdatePacket::entityId,
        Vec3.STREAM_CODEC,
        LimbSwingUpdatePacket::position,
        ByteBufCodecs.FLOAT,
        LimbSwingUpdatePacket::limbSwing,
        LimbSwingUpdatePacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<LimbSwingUpdatePacket> type() {
        return AllPackets.LIMBSWING_UPDATE;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, LimbSwingUpdatePacket> callback() {
        return AllClientHandle::onLimbSwingUpdate;
    }
}
