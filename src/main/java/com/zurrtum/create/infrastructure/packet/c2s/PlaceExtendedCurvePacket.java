package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record PlaceExtendedCurvePacket(boolean mainHand, boolean ctrlDown) implements C2SPacket {
    public static final StreamCodec<ByteBuf, PlaceExtendedCurvePacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        PlaceExtendedCurvePacket::mainHand,
        ByteBufCodecs.BOOL,
        PlaceExtendedCurvePacket::ctrlDown,
        PlaceExtendedCurvePacket::new
    );

    @Override
    public PacketType<PlaceExtendedCurvePacket> type() {
        return AllPackets.PLACE_CURVED_TRACK;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, PlaceExtendedCurvePacket> callback() {
        return AllHandle::onPlaceExtendedCurve;
    }
}
