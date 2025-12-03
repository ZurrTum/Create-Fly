package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record ConfigureThresholdSwitchPacket(
    BlockPos pos, int offBelow, int onAbove, boolean invert, boolean inStacks
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, ConfigureThresholdSwitchPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        ConfigureThresholdSwitchPacket::pos,
        PacketCodecs.INTEGER,
        ConfigureThresholdSwitchPacket::offBelow,
        PacketCodecs.INTEGER,
        ConfigureThresholdSwitchPacket::onAbove,
        PacketCodecs.BOOLEAN,
        ConfigureThresholdSwitchPacket::invert,
        PacketCodecs.BOOLEAN,
        ConfigureThresholdSwitchPacket::inStacks,
        ConfigureThresholdSwitchPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onConfigureThresholdSwitch((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<ConfigureThresholdSwitchPacket> getPacketType() {
        return AllPackets.CONFIGURE_STOCKSWITCH;
    }
}
