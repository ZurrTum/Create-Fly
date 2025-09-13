package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record BlueprintAssignCompleteRecipePacket(NetworkRecipeId recipeId) implements C2SPacket {
    public static final PacketCodec<ByteBuf, BlueprintAssignCompleteRecipePacket> CODEC = NetworkRecipeId.PACKET_CODEC.xmap(BlueprintAssignCompleteRecipePacket::new,
        BlueprintAssignCompleteRecipePacket::recipeId
    );

    @Override
    public PacketType<BlueprintAssignCompleteRecipePacket> getPacketType() {
        return AllPackets.BLUEPRINT_COMPLETE_RECIPE;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, BlueprintAssignCompleteRecipePacket> callback() {
        return AllHandle::onBlueprintAssignCompleteRecipe;
    }
}
