package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record PlaceExtendedCurvePacket(boolean mainHand, boolean ctrlDown) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, PlaceExtendedCurvePacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        PlaceExtendedCurvePacket::mainHand,
        ByteBufCodecs.BOOL,
        PlaceExtendedCurvePacket::ctrlDown,
        PlaceExtendedCurvePacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onPlaceExtendedCurve((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<PlaceExtendedCurvePacket> type() {
        return AllPackets.PLACE_CURVED_TRACK;
    }
}
