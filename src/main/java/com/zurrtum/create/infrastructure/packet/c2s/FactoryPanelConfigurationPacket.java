package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipLargerStreamCodecs;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public record FactoryPanelConfigurationPacket(
    FactoryPanelPosition position, String address, Map<FactoryPanelPosition, Integer> inputAmounts, List<ItemStack> craftingArrangement,
    int outputAmount, int promiseClearingInterval, FactoryPanelPosition removeConnection, boolean clearPromises, boolean reset, boolean redstoneReset
) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, FactoryPanelConfigurationPacket> CODEC = CatnipLargerStreamCodecs.composite(
        FactoryPanelPosition.PACKET_CODEC,
        FactoryPanelConfigurationPacket::position,
        ByteBufCodecs.STRING_UTF8,
        FactoryPanelConfigurationPacket::address,
        ByteBufCodecs.map(HashMap::new, FactoryPanelPosition.PACKET_CODEC, ByteBufCodecs.INT),
        FactoryPanelConfigurationPacket::inputAmounts,
        ItemStack.OPTIONAL_LIST_STREAM_CODEC,
        FactoryPanelConfigurationPacket::craftingArrangement,
        ByteBufCodecs.VAR_INT,
        FactoryPanelConfigurationPacket::outputAmount,
        ByteBufCodecs.VAR_INT,
        FactoryPanelConfigurationPacket::promiseClearingInterval,
        CatnipStreamCodecBuilders.nullable(FactoryPanelPosition.PACKET_CODEC),
        FactoryPanelConfigurationPacket::removeConnection,
        ByteBufCodecs.BOOL,
        FactoryPanelConfigurationPacket::clearPromises,
        ByteBufCodecs.BOOL,
        FactoryPanelConfigurationPacket::reset,
        ByteBufCodecs.BOOL,
        FactoryPanelConfigurationPacket::redstoneReset,
        FactoryPanelConfigurationPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<FactoryPanelConfigurationPacket> type() {
        return AllPackets.CONFIGURE_FACTORY_PANEL;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, FactoryPanelConfigurationPacket> callback() {
        return AllHandle::onFactoryPanelConfiguration;
    }
}
