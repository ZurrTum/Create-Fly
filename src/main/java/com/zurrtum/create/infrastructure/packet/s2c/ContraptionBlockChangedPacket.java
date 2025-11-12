package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.util.TriConsumer;

public record ContraptionBlockChangedPacket(int entityId, BlockPos localPos, BlockState newState) implements S2CPacket {
    public static final StreamCodec<ByteBuf, ContraptionBlockChangedPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        ContraptionBlockChangedPacket::entityId,
        BlockPos.STREAM_CODEC,
        ContraptionBlockChangedPacket::localPos,
        CatnipStreamCodecs.BLOCK_STATE,
        ContraptionBlockChangedPacket::newState,
        ContraptionBlockChangedPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ContraptionBlockChangedPacket> callback() {
        return AllClientHandle::onContraptionBlockChanged;
    }

    @Override
    public PacketType<ContraptionBlockChangedPacket> type() {
        return AllPackets.CONTRAPTION_BLOCK_CHANGED;
    }
}
