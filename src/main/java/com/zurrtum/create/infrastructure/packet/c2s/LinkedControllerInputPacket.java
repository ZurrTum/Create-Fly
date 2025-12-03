package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.List;

public record LinkedControllerInputPacket(
    List<Integer> activatedButtons, boolean press, BlockPos lecternPos
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, LinkedControllerInputPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER.collect(PacketCodecs.toList()),
        LinkedControllerInputPacket::activatedButtons,
        PacketCodecs.BOOLEAN,
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
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onLinkedControllerInput((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<LinkedControllerInputPacket> getPacketType() {
        return AllPackets.LINKED_CONTROLLER_INPUT;
    }
}
