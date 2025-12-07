package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.List;

public record BlueprintAssignCompleteRecipePacket(List<ItemStack> input, ItemStack output) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, BlueprintAssignCompleteRecipePacket> CODEC = PacketCodec.tuple(
        ItemStack.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList()),
        BlueprintAssignCompleteRecipePacket::input,
        ItemStack.PACKET_CODEC,
        BlueprintAssignCompleteRecipePacket::output,
        BlueprintAssignCompleteRecipePacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onBlueprintAssignCompleteRecipe((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<BlueprintAssignCompleteRecipePacket> getPacketType() {
        return AllPackets.BLUEPRINT_COMPLETE_RECIPE;
    }
}
