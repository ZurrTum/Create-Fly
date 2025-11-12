package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ControlsInputPacket(
    List<Integer> activatedButtons, boolean press, int contraptionEntityId, BlockPos controlsPos, boolean stopControlling
) implements C2SPacket {
    public static final StreamCodec<ByteBuf, ControlsInputPacket> CODEC = StreamCodec.composite(
        CatnipStreamCodecBuilders.list(ByteBufCodecs.VAR_INT),
        ControlsInputPacket::activatedButtons,
        ByteBufCodecs.BOOL,
        ControlsInputPacket::press,
        ByteBufCodecs.INT,
        ControlsInputPacket::contraptionEntityId,
        BlockPos.STREAM_CODEC,
        ControlsInputPacket::controlsPos,
        ByteBufCodecs.BOOL,
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
    public PacketType<ControlsInputPacket> type() {
        return AllPackets.CONTROLS_INPUT;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ControlsInputPacket> callback() {
        return AllHandle::onControlsInput;
    }
}
