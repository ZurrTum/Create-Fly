package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record SymmetryEffectPacket(BlockPos mirror, List<BlockPos> positions) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, SymmetryEffectPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        SymmetryEffectPacket::mirror,
        CatnipStreamCodecBuilders.list(BlockPos.PACKET_CODEC),
        SymmetryEffectPacket::positions,
        SymmetryEffectPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onSymmetryEffect(listener, this);
    }

    @Override
    public PacketType<SymmetryEffectPacket> getPacketType() {
        return AllPackets.SYMMETRY_EFFECT;
    }
}
