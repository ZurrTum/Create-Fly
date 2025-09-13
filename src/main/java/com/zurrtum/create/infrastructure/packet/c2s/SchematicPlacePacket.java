package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record SchematicPlacePacket(ItemStack stack) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, SchematicPlacePacket> CODEC = ItemStack.PACKET_CODEC.xmap(
        SchematicPlacePacket::new,
        SchematicPlacePacket::stack
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<SchematicPlacePacket> getPacketType() {
        return AllPackets.PLACE_SCHEMATIC;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, SchematicPlacePacket> callback() {
        return AllHandle::onSchematicPlace;
    }
}
