package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record BlueprintPreviewRequestPacket(int entityId, int index, boolean sneaking) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, BlueprintPreviewRequestPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        BlueprintPreviewRequestPacket::entityId,
        PacketCodecs.VAR_INT,
        BlueprintPreviewRequestPacket::index,
        PacketCodecs.BOOLEAN,
        BlueprintPreviewRequestPacket::sneaking,
        BlueprintPreviewRequestPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, BlueprintPreviewRequestPacket> callback() {
        return AllHandle::onBlueprintPreviewRequest;
    }

    @Override
    public PacketType<? extends BlueprintPreviewRequestPacket> getPacketType() {
        return AllPackets.REQUEST_BLUEPRINT_PREVIEW;
    }
}
