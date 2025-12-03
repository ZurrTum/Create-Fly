package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public record ZapperBeamPacket(Vec3d location, Hand hand, boolean self, Vec3d target) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, ZapperBeamPacket> CODEC = PacketCodec.tuple(
        Vec3d.PACKET_CODEC,
        ZapperBeamPacket::location,
        CatnipStreamCodecs.HAND,
        ZapperBeamPacket::hand,
        PacketCodecs.BOOLEAN,
        ZapperBeamPacket::self,
        Vec3d.PACKET_CODEC,
        ZapperBeamPacket::target,
        ZapperBeamPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onZapperBeam(listener, this);
    }

    @Override
    public PacketType<ZapperBeamPacket> getPacketType() {
        return AllPackets.BEAM_EFFECT;
    }
}
