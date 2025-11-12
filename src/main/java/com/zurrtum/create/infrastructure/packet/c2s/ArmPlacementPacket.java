package com.zurrtum.create.infrastructure.packet.c2s;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ArmPlacementPacket(ListTag tag, BlockPos pos) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmPlacementPacket> CODEC = StreamCodec.composite(
        CatnipStreamCodecs.COMPOUND_LIST_TAG,
        ArmPlacementPacket::tag,
        BlockPos.STREAM_CODEC,
        ArmPlacementPacket::pos,
        ArmPlacementPacket::new
    );

    public ArmPlacementPacket(List<ArmInteractionPoint> points, BlockPos pos) {
        this(new ListTag(), pos);
        Codec<ArmInteractionPoint> codec = ArmInteractionPoint.getCodec(null, pos);
        ArmBlockEntity.appendEncodedPoints(points, codec, this.tag);
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ArmPlacementPacket> type() {
        return AllPackets.PLACE_ARM;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ArmPlacementPacket> callback() {
        return AllHandle::onArmPlacement;
    }
}
