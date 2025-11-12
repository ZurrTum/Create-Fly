package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

public record ZapperBeamPacket(Vec3 location, InteractionHand hand, boolean self, Vec3 target) implements ShootGadgetPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ZapperBeamPacket> CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC,
        ZapperBeamPacket::location,
        CatnipStreamCodecs.HAND,
        ZapperBeamPacket::hand,
        ByteBufCodecs.BOOL,
        ZapperBeamPacket::self,
        Vec3.STREAM_CODEC,
        ZapperBeamPacket::target,
        ZapperBeamPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ZapperBeamPacket> type() {
        return AllPackets.BEAM_EFFECT;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ZapperBeamPacket> callback() {
        return AllClientHandle::onZapperBeam;
    }
}
