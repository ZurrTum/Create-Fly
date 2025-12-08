package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

import java.util.List;

public record SymmetryEffectPacket(BlockPos mirror, List<BlockPos> positions) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, SymmetryEffectPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        SymmetryEffectPacket::mirror,
        CatnipStreamCodecBuilders.list(BlockPos.STREAM_CODEC),
        SymmetryEffectPacket::positions,
        SymmetryEffectPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onSymmetryEffect(listener, this);
    }

    @Override
    public PacketType<SymmetryEffectPacket> type() {
        return AllPackets.SYMMETRY_EFFECT;
    }
}
