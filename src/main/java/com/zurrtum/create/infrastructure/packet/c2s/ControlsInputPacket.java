package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public record ControlsInputPacket(
    List<Integer> activatedButtons, boolean press, int contraptionEntityId, BlockPos controlsPos, boolean stopControlling
) implements C2SPacket {
    public static final PacketCodec<ByteBuf, ControlsInputPacket> CODEC = PacketCodec.tuple(
        CatnipStreamCodecBuilders.list(PacketCodecs.VAR_INT),
        ControlsInputPacket::activatedButtons,
        PacketCodecs.BOOLEAN,
        ControlsInputPacket::press,
        PacketCodecs.INTEGER,
        ControlsInputPacket::contraptionEntityId,
        BlockPos.PACKET_CODEC,
        ControlsInputPacket::controlsPos,
        PacketCodecs.BOOLEAN,
        ControlsInputPacket::stopControlling,
        ControlsInputPacket::new
    );

    public ControlsInputPacket(
        Collection<Integer> activatedButtons,
        boolean press,
        int contraptionEntityId,
        BlockPos controlsPos,
        boolean stopControlling
    ) {
        // given list is reused, copy it
        this(List.copyOf(activatedButtons), press, contraptionEntityId, controlsPos, stopControlling);
    }

    @Override
    public PacketType<ControlsInputPacket> getPacketType() {
        return AllPackets.CONTROLS_INPUT;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ControlsInputPacket> callback() {
        return AllHandle::onControlsInput;
    }
}
