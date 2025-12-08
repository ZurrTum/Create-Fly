package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public record GhostItemSubmitPacket(ItemStack item, int slot) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, GhostItemSubmitPacket> CODEC = StreamCodec.composite(
        ItemStack.OPTIONAL_STREAM_CODEC,
        GhostItemSubmitPacket::item,
        ByteBufCodecs.INT,
        GhostItemSubmitPacket::slot,
        GhostItemSubmitPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onGhostItemSubmit((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<GhostItemSubmitPacket> type() {
        return AllPackets.SUBMIT_GHOST_ITEM;
    }
}
