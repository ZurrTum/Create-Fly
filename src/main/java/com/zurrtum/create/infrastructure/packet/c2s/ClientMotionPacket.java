package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;

public record ClientMotionPacket(Vec3d motion, boolean onGround, float limbSwing) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, ClientMotionPacket> CODEC = PacketCodec.tuple(
        Vec3d.PACKET_CODEC,
        ClientMotionPacket::motion,
        PacketCodecs.BOOLEAN,
        ClientMotionPacket::onGround,
        PacketCodecs.FLOAT,
        ClientMotionPacket::limbSwing,
        ClientMotionPacket::new
    );

    @Override
    public PacketType<ClientMotionPacket> getPacketType() {
        return AllPackets.CLIENT_MOTION;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ClientMotionPacket> callback() {
        return AllHandle::onClientMotion;
    }
}
