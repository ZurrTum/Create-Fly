package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.foundation.codec.CreateStreamCodecs;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.apache.logging.log4j.util.TriConsumer;

public record OpenScreenPacket(int id, MenuType<?> type, Text name, byte[] data) implements S2CPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, OpenScreenPacket> callback() {
        return AllClientHandle::onOpenScreen;
    }

    @Override
    public PacketType<OpenScreenPacket> getPacketType() {
        return AllPackets.OPEN_SCREEN;
    }
}
