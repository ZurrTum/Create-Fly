package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipLargerStreamCodecs;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.function.BiConsumer;

public record ValueSettingsPacket(
    BlockPos pos, int row, int value, Hand interactHand, BlockHitResult hitResult, Direction side, boolean ctrlDown, int behaviourIndex
) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, ValueSettingsPacket> CODEC = CatnipLargerStreamCodecs.composite(
        BlockPos.PACKET_CODEC,
        ValueSettingsPacket::pos,
        PacketCodecs.VAR_INT,
        ValueSettingsPacket::row,
        PacketCodecs.VAR_INT,
        ValueSettingsPacket::value,
        CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.HAND),
        ValueSettingsPacket::interactHand,
        CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.BLOCK_HIT_RESULT),
        ValueSettingsPacket::hitResult,
        Direction.PACKET_CODEC,
        ValueSettingsPacket::side,
        PacketCodecs.BOOLEAN,
        ValueSettingsPacket::ctrlDown,
        PacketCodecs.VAR_INT,
        ValueSettingsPacket::behaviourIndex,
        ValueSettingsPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ValueSettingsPacket> getPacketType() {
        return AllPackets.VALUE_SETTINGS;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ValueSettingsPacket> callback() {
        return AllHandle::onValueSettings;
    }
}
