package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public record BlueprintAssignCompleteRecipePacket(NetworkRecipeId recipeId) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, BlueprintAssignCompleteRecipePacket> CODEC = NetworkRecipeId.PACKET_CODEC.xmap(BlueprintAssignCompleteRecipePacket::new,
        BlueprintAssignCompleteRecipePacket::recipeId
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
