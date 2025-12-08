package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ConfigureThresholdSwitchPacket(
    BlockPos pos, int offBelow, int onAbove, boolean invert, boolean inStacks
) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureThresholdSwitchPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ConfigureThresholdSwitchPacket::pos,
        ByteBufCodecs.INT,
        ConfigureThresholdSwitchPacket::offBelow,
        ByteBufCodecs.INT,
        ConfigureThresholdSwitchPacket::onAbove,
        ByteBufCodecs.BOOL,
        ConfigureThresholdSwitchPacket::invert,
        ByteBufCodecs.BOOL,
        ConfigureThresholdSwitchPacket::inStacks,
        ConfigureThresholdSwitchPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onConfigureThresholdSwitch((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ConfigureThresholdSwitchPacket> type() {
        return AllPackets.CONFIGURE_STOCKSWITCH;
    }
}
