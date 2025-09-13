package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record PlaceExtendedCurvePacket(boolean mainHand, boolean ctrlDown) implements C2SPacket {
    public static final PacketCodec<ByteBuf, PlaceExtendedCurvePacket> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN,
        PlaceExtendedCurvePacket::mainHand,
        PacketCodecs.BOOLEAN,
        PlaceExtendedCurvePacket::ctrlDown,
        PlaceExtendedCurvePacket::new
    );

    @Override
    public PacketType<PlaceExtendedCurvePacket> getPacketType() {
        return AllPackets.PLACE_CURVED_TRACK;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, PlaceExtendedCurvePacket> callback() {
        return AllHandle::onPlaceExtendedCurve;
    }
}
