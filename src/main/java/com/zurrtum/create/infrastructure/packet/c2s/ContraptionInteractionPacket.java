package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.function.BiConsumer;

public record ContraptionInteractionPacket(Hand hand, int target, BlockPos localPos, Direction face) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, ContraptionInteractionPacket> CODEC = PacketCodec.tuple(
        CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.HAND),
        ContraptionInteractionPacket::hand,
        PacketCodecs.INTEGER,
        ContraptionInteractionPacket::target,
        BlockPos.PACKET_CODEC,
        ContraptionInteractionPacket::localPos,
        Direction.PACKET_CODEC,
        ContraptionInteractionPacket::face,
        ContraptionInteractionPacket::new
    );

    public ContraptionInteractionPacket(AbstractContraptionEntity target, Hand hand, BlockPos localPos, Direction side) {
        this(hand, target.getId(), localPos, side);
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ContraptionInteractionPacket> getPacketType() {
        return AllPackets.CONTRAPTION_INTERACT;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ContraptionInteractionPacket> callback() {
        return AllHandle::onContraptionInteraction;
    }
}
