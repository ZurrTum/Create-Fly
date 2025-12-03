package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipLargerStreamCodecs;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record FactoryPanelConfigurationPacket(
    FactoryPanelPosition position, String address, Map<FactoryPanelPosition, Integer> inputAmounts, List<ItemStack> craftingArrangement,
    int outputAmount, int promiseClearingInterval, FactoryPanelPosition removeConnection, boolean clearPromises, boolean reset, boolean redstoneReset
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, FactoryPanelConfigurationPacket> CODEC = CatnipLargerStreamCodecs.composite(
        FactoryPanelPosition.PACKET_CODEC,
        FactoryPanelConfigurationPacket::position,
        PacketCodecs.STRING,
        FactoryPanelConfigurationPacket::address,
        PacketCodecs.map(HashMap::new, FactoryPanelPosition.PACKET_CODEC, PacketCodecs.INTEGER),
        FactoryPanelConfigurationPacket::inputAmounts,
        ItemStack.OPTIONAL_LIST_PACKET_CODEC,
        FactoryPanelConfigurationPacket::craftingArrangement,
        PacketCodecs.VAR_INT,
        FactoryPanelConfigurationPacket::outputAmount,
        PacketCodecs.VAR_INT,
        FactoryPanelConfigurationPacket::promiseClearingInterval,
        CatnipStreamCodecBuilders.nullable(FactoryPanelPosition.PACKET_CODEC),
        FactoryPanelConfigurationPacket::removeConnection,
        PacketCodecs.BOOLEAN,
        FactoryPanelConfigurationPacket::clearPromises,
        PacketCodecs.BOOLEAN,
        FactoryPanelConfigurationPacket::reset,
        PacketCodecs.BOOLEAN,
        FactoryPanelConfigurationPacket::redstoneReset,
        FactoryPanelConfigurationPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onFactoryPanelConfiguration((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<FactoryPanelConfigurationPacket> getPacketType() {
        return AllPackets.CONFIGURE_FACTORY_PANEL;
    }
}
