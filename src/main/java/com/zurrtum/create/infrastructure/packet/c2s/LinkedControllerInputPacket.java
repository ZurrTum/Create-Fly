package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Collection;
import java.util.List;

public record LinkedControllerInputPacket(
    List<Integer> activatedButtons, boolean press, BlockPos lecternPos
) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, LinkedControllerInputPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
        LinkedControllerInputPacket::activatedButtons,
        ByteBufCodecs.BOOL,
        LinkedControllerInputPacket::press,
        CatnipStreamCodecs.NULLABLE_BLOCK_POS,
        LinkedControllerInputPacket::lecternPos,
        LinkedControllerInputPacket::new
    );

    public LinkedControllerInputPacket(Collection<Integer> activatedButtons, boolean press) {
        this(activatedButtons, press, null);
    }

    public LinkedControllerInputPacket(Collection<Integer> activatedButtons, boolean press, BlockPos lecternPos) {
        this(List.copyOf(activatedButtons), press, lecternPos);
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onLinkedControllerInput((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<LinkedControllerInputPacket> type() {
        return AllPackets.LINKED_CONTROLLER_INPUT;
    }
}
