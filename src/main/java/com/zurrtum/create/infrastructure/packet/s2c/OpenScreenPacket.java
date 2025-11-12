package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.foundation.codec.CreateStreamCodecs;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record OpenScreenPacket(int id, MenuType<?> menu, Component name, byte[] data) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreenPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.CONTAINER_ID,
        OpenScreenPacket::id,
        ByteBufCodecs.registry(CreateRegistryKeys.MENU_TYPE),
        OpenScreenPacket::menu,
        ComponentSerialization.TRUSTED_STREAM_CODEC,
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
    public PacketType<OpenScreenPacket> type() {
        return AllPackets.OPEN_SCREEN;
    }
}
