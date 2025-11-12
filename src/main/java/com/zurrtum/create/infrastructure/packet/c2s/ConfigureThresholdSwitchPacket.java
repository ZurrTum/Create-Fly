package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ConfigureThresholdSwitchPacket(
    BlockPos pos, int offBelow, int onAbove, boolean invert, boolean inStacks
) implements C2SPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ConfigureThresholdSwitchPacket> type() {
        return AllPackets.CONFIGURE_STOCKSWITCH;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ConfigureThresholdSwitchPacket> callback() {
        return AllHandle::onConfigureThresholdSwitch;
    }
}
