package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.foundation.codec.CreateStreamCodecs;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record OpenScreenPacket(int id, MenuType<?> type, Text name, byte[] data) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, OpenScreenPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.SYNC_ID,
        OpenScreenPacket::id,
        PacketCodecs.registryValue(CreateRegistryKeys.MENU_TYPE),
        OpenScreenPacket::type,
        TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC,
        OpenScreenPacket::name,
        CreateStreamCodecs.UNBOUNDED_BYTE_ARRAY,
        OpenScreenPacket::data,
        OpenScreenPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onOpenScreen(listener, this);
    }

    @Override
    public PacketType<OpenScreenPacket> getPacketType() {
        return AllPackets.OPEN_SCREEN;
    }
}
