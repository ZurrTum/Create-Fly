package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
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

public record ContraptionInteractionPacket(
    InteractionHand hand, int target, BlockPos localPos, Direction face
) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionInteractionPacket> CODEC = StreamCodec.composite(
        CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.HAND),
        ContraptionInteractionPacket::hand,
        ByteBufCodecs.INT,
        ContraptionInteractionPacket::target,
        BlockPos.STREAM_CODEC,
        ContraptionInteractionPacket::localPos,
        Direction.STREAM_CODEC,
        ContraptionInteractionPacket::face,
        ContraptionInteractionPacket::new
    );

    public ContraptionInteractionPacket(AbstractContraptionEntity target, InteractionHand hand, BlockPos localPos, Direction side) {
        this(hand, target.getId(), localPos, side);
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onContraptionInteraction((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ContraptionInteractionPacket> type() {
        return AllPackets.CONTRAPTION_INTERACT;
    }
}
