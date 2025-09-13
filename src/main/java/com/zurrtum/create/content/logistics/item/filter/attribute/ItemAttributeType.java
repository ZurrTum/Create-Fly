package com.zurrtum.create.content.logistics.item.filter.attribute;

import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ItemAttributeType {
    @NotNull ItemAttribute createAttribute();

    List<ItemAttribute> getAllAttributes(ItemStack stack, World level);

    MapCodec<? extends ItemAttribute> codec();

    PacketCodec<? super RegistryByteBuf, ? extends ItemAttribute> packetCodec();
}
