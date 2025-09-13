package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record GhostItemSubmitPacket(ItemStack item, int slot) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, GhostItemSubmitPacket> CODEC = PacketCodec.tuple(
        ItemStack.OPTIONAL_PACKET_CODEC,
        GhostItemSubmitPacket::item,
        PacketCodecs.INTEGER,
        GhostItemSubmitPacket::slot,
        GhostItemSubmitPacket::new
    );

    @Override
    public PacketType<GhostItemSubmitPacket> getPacketType() {
        return AllPackets.SUBMIT_GHOST_ITEM;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, GhostItemSubmitPacket> callback() {
        return AllHandle::onGhostItemSubmit;
    }
}
