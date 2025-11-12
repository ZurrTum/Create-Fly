package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import java.util.function.BiConsumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public record SchematicPlacePacket(ItemStack stack) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, SchematicPlacePacket> CODEC = ItemStack.STREAM_CODEC.map(
        SchematicPlacePacket::new,
        SchematicPlacePacket::stack
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<SchematicPlacePacket> type() {
        return AllPackets.PLACE_SCHEMATIC;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, SchematicPlacePacket> callback() {
        return AllHandle::onSchematicPlace;
    }
}
