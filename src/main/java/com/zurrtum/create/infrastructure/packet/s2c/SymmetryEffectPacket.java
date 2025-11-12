package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record SymmetryEffectPacket(BlockPos mirror, List<BlockPos> positions) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, SymmetryEffectPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        SymmetryEffectPacket::mirror,
        CatnipStreamCodecBuilders.list(BlockPos.STREAM_CODEC),
        SymmetryEffectPacket::positions,
        SymmetryEffectPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<SymmetryEffectPacket> type() {
        return AllPackets.SYMMETRY_EFFECT;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, SymmetryEffectPacket> callback() {
        return AllClientHandle::onSymmetryEffect;
    }
}
