package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.decoration.slidingDoor.DoorControl;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record StationEditPacket(
    BlockPos pos, boolean dropSchedule, boolean assemblyMode, Boolean tryAssemble, DoorControl doorControl, String name
) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, StationEditPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        StationEditPacket::pos,
        ByteBufCodecs.BOOL,
        StationEditPacket::dropSchedule,
        ByteBufCodecs.BOOL,
        StationEditPacket::assemblyMode,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.BOOL),
        StationEditPacket::tryAssemble,
        CatnipStreamCodecBuilders.nullable(DoorControl.STREAM_CODEC),
        StationEditPacket::doorControl,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.stringUtf8(256)),
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
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onStationEdit((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<StationEditPacket> type() {
        return AllPackets.CONFIGURE_STATION;
    }
}
