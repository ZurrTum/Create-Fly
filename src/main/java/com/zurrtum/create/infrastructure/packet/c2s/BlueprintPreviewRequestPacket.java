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

public record BlueprintPreviewRequestPacket(int entityId, int index, boolean sneaking) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintPreviewRequestPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        BlueprintPreviewRequestPacket::entityId,
        ByteBufCodecs.VAR_INT,
        BlueprintPreviewRequestPacket::index,
        ByteBufCodecs.BOOL,
        BlueprintPreviewRequestPacket::sneaking,
        BlueprintPreviewRequestPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onBlueprintPreviewRequest((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<? extends BlueprintPreviewRequestPacket> type() {
        return AllPackets.REQUEST_BLUEPRINT_PREVIEW;
    }
}
