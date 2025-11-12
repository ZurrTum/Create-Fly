package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

public record BlueprintAssignCompleteRecipePacket(RecipeDisplayId recipeId) implements C2SPacket {
    public static final StreamCodec<ByteBuf, BlueprintAssignCompleteRecipePacket> CODEC = RecipeDisplayId.STREAM_CODEC.map(BlueprintAssignCompleteRecipePacket::new,
        BlueprintAssignCompleteRecipePacket::recipeId
    );

    @Override
    public PacketType<BlueprintAssignCompleteRecipePacket> type() {
        return AllPackets.BLUEPRINT_COMPLETE_RECIPE;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, BlueprintAssignCompleteRecipePacket> callback() {
        return AllHandle::onBlueprintAssignCompleteRecipe;
    }
}
