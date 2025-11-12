package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import java.util.function.BiConsumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public record GhostItemSubmitPacket(ItemStack item, int slot) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, GhostItemSubmitPacket> CODEC = StreamCodec.composite(
        ItemStack.OPTIONAL_STREAM_CODEC,
        GhostItemSubmitPacket::item,
        ByteBufCodecs.INT,
        GhostItemSubmitPacket::slot,
        GhostItemSubmitPacket::new
    );

    @Override
    public PacketType<GhostItemSubmitPacket> type() {
        return AllPackets.SUBMIT_GHOST_ITEM;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, GhostItemSubmitPacket> callback() {
        return AllHandle::onGhostItemSubmit;
    }
}
