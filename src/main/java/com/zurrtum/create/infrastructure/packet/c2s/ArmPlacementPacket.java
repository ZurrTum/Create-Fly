package com.zurrtum.create.infrastructure.packet.c2s;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record ArmPlacementPacket(NbtList tag, BlockPos pos) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, ArmPlacementPacket> CODEC = PacketCodec.tuple(
        CatnipStreamCodecs.COMPOUND_LIST_TAG,
        ArmPlacementPacket::tag,
        BlockPos.PACKET_CODEC,
        ArmPlacementPacket::pos,
        ArmPlacementPacket::new
    );

    public ArmPlacementPacket(List<ArmInteractionPoint> points, BlockPos pos) {
        this(new NbtList(), pos);
        Codec<ArmInteractionPoint> codec = ArmInteractionPoint.getCodec(null, pos);
        ArmBlockEntity.appendEncodedPoints(points, codec, this.tag);
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onArmPlacement((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<ArmPlacementPacket> getPacketType() {
        return AllPackets.PLACE_ARM;
    }
}
