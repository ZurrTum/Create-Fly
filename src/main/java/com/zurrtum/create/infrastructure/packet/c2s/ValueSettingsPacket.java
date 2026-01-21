package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipLargerStreamCodecs;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public record ValueSettingsPacket(
    BlockPos pos, int row, int value, @Nullable InteractionHand interactHand, @Nullable BlockHitResult hitResult, Direction side, boolean ctrlDown,
    int behaviourIndex
) implements Packet<ServerGamePacketListener> {
    @SuppressWarnings("DataFlowIssue")
    public static final StreamCodec<RegistryFriendlyByteBuf, ValueSettingsPacket> CODEC = CatnipLargerStreamCodecs.composite(
        BlockPos.STREAM_CODEC,
        ValueSettingsPacket::pos,
        ByteBufCodecs.VAR_INT,
        ValueSettingsPacket::row,
        ByteBufCodecs.VAR_INT,
        ValueSettingsPacket::value,
        CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.HAND),
        ValueSettingsPacket::interactHand,
        CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.BLOCK_HIT_RESULT),
        ValueSettingsPacket::hitResult,
        Direction.STREAM_CODEC,
        ValueSettingsPacket::side,
        ByteBufCodecs.BOOL,
        ValueSettingsPacket::ctrlDown,
        ByteBufCodecs.VAR_INT,
        ValueSettingsPacket::behaviourIndex,
        ValueSettingsPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onValueSettings((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ValueSettingsPacket> type() {
        return AllPackets.VALUE_SETTINGS;
    }
}
