package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

public record ClientMotionPacket(Vec3 motion, boolean onGround, float limbSwing) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientMotionPacket> CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC,
        ClientMotionPacket::motion,
        ByteBufCodecs.BOOL,
        ClientMotionPacket::onGround,
        ByteBufCodecs.FLOAT,
        ClientMotionPacket::limbSwing,
        ClientMotionPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onClientMotion((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ClientMotionPacket> type() {
        return AllPackets.CLIENT_MOTION;
    }
}
