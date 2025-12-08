package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.phys.Vec3;

public record LimbSwingUpdatePacket(int entityId, Vec3 position, float limbSwing) implements Packet<ClientGamePacketListener> {
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
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onLimbSwingUpdate(listener, this);
    }

    @Override
    public PacketType<LimbSwingUpdatePacket> type() {
        return AllPackets.LIMBSWING_UPDATE;
    }
}
