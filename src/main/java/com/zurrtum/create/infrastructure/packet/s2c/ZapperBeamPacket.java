package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.util.TriConsumer;

public record ZapperBeamPacket(Vec3d location, Hand hand, boolean self, Vec3d target) implements ShootGadgetPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ZapperBeamPacket> getPacketType() {
        return AllPackets.BEAM_EFFECT;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ZapperBeamPacket> callback() {
        return AllClientHandle::onZapperBeam;
    }
}
