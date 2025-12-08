package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;

public record ConfigureSymmetryWandPacket(InteractionHand hand, SymmetryMirror mirror) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ConfigureSymmetryWandPacket> CODEC = StreamCodec.composite(
        CatnipStreamCodecs.HAND,
        ConfigureSymmetryWandPacket::hand,
        SymmetryMirror.STREAM_CODEC,
        ConfigureSymmetryWandPacket::mirror,
        ConfigureSymmetryWandPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onConfigureSymmetryWand((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ConfigureSymmetryWandPacket> type() {
        return AllPackets.CONFIGURE_SYMMETRY_WAND;
    }
}
