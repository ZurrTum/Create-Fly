package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.decoration.slidingDoor.DoorControl;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record StationEditPacket(
    BlockPos pos, boolean dropSchedule, boolean assemblyMode, Boolean tryAssemble, DoorControl doorControl, String name
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, StationEditPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        StationEditPacket::pos,
        PacketCodecs.BOOLEAN,
        StationEditPacket::dropSchedule,
        PacketCodecs.BOOLEAN,
        StationEditPacket::assemblyMode,
        CatnipStreamCodecBuilders.nullable(PacketCodecs.BOOLEAN),
        StationEditPacket::tryAssemble,
        CatnipStreamCodecBuilders.nullable(DoorControl.STREAM_CODEC),
        StationEditPacket::doorControl,
        CatnipStreamCodecBuilders.nullable(PacketCodecs.string(256)),
        StationEditPacket::name,
        StationEditPacket::new
    );

    public static StationEditPacket dropSchedule(BlockPos pos) {
        return new StationEditPacket(pos, true, false, false, null, null);
    }

    public static StationEditPacket tryAssemble(BlockPos pos) {
        return new StationEditPacket(pos, false, false, true, null, null);
    }

    public static StationEditPacket tryDisassemble(BlockPos pos) {
        return new StationEditPacket(pos, false, false, false, null, null);
    }

    public static StationEditPacket configure(BlockPos pos, boolean assemble, String name, DoorControl doorControl) {
        return new StationEditPacket(pos, false, assemble, null, doorControl, name);
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onStationEdit((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<StationEditPacket> getPacketType() {
        return AllPackets.CONFIGURE_STATION;
    }
}
