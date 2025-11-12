package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;

import java.util.function.BiConsumer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;

public record ConfigureSymmetryWandPacket(InteractionHand hand, SymmetryMirror mirror) implements C2SPacket {
    public static final StreamCodec<FriendlyByteBuf, ConfigureSymmetryWandPacket> CODEC = StreamCodec.composite(
        CatnipStreamCodecs.HAND,
        ConfigureSymmetryWandPacket::hand,
        SymmetryMirror.STREAM_CODEC,
        ConfigureSymmetryWandPacket::mirror,
        ConfigureSymmetryWandPacket::new
    );

    @Override
    public PacketType<ConfigureSymmetryWandPacket> type() {
        return AllPackets.CONFIGURE_SYMMETRY_WAND;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ConfigureSymmetryWandPacket> callback() {
        return AllHandle::onConfigureSymmetryWand;
    }
}
