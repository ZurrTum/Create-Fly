package com.zurrtum.create.infrastructure.packet.s2c;

import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record TunnelFlapPacket(BlockPos pos, List<Pair<Direction, Boolean>> flaps) implements S2CPacket {
    public static final StreamCodec<ByteBuf, TunnelFlapPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        TunnelFlapPacket::pos,
        CatnipStreamCodecBuilders.list(CatnipStreamCodecBuilders.pair(Direction.STREAM_CODEC, ByteBufCodecs.BOOL)),
        TunnelFlapPacket::flaps,
        TunnelFlapPacket::new
    );

    public TunnelFlapPacket(BeltTunnelBlockEntity blockEntity, List<Pair<Direction, Boolean>> flaps) {
        this(blockEntity.getBlockPos(), new ArrayList<>(flaps));
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, TunnelFlapPacket> callback() {
        return AllClientHandle::onTunnelFlap;
    }

    @Override
    public PacketType<TunnelFlapPacket> type() {
        return AllPackets.TUNNEL_FLAP;
    }
}
