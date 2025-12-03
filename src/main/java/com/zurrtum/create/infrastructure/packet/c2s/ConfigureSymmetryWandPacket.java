package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Hand;

public record ConfigureSymmetryWandPacket(Hand hand, SymmetryMirror mirror) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, ConfigureSymmetryWandPacket> CODEC = PacketCodec.tuple(
        CatnipStreamCodecs.HAND,
        ConfigureSymmetryWandPacket::hand,
        SymmetryMirror.STREAM_CODEC,
        ConfigureSymmetryWandPacket::mirror,
        ConfigureSymmetryWandPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onConfigureSymmetryWand((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<ConfigureSymmetryWandPacket> getPacketType() {
        return AllPackets.CONFIGURE_SYMMETRY_WAND;
    }
}
