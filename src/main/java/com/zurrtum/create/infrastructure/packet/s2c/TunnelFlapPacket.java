package com.zurrtum.create.infrastructure.packet.s2c;

import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;

public record TunnelFlapPacket(BlockPos pos, List<Pair<Direction, Boolean>> flaps) implements S2CPacket {
    public static final PacketCodec<ByteBuf, TunnelFlapPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        TunnelFlapPacket::pos,
        CatnipStreamCodecBuilders.list(CatnipStreamCodecBuilders.pair(Direction.PACKET_CODEC, PacketCodecs.BOOLEAN)),
        TunnelFlapPacket::flaps,
        TunnelFlapPacket::new
    );

    public TunnelFlapPacket(BeltTunnelBlockEntity blockEntity, List<Pair<Direction, Boolean>> flaps) {
        this(blockEntity.getPos(), new ArrayList<>(flaps));
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, TunnelFlapPacket> callback() {
        return AllClientHandle::onTunnelFlap;
    }

    @Override
    public PacketType<TunnelFlapPacket> getPacketType() {
        return AllPackets.TUNNEL_FLAP;
    }
}
