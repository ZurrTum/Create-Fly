package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.Objects;

public record SandPaperItemComponent(ItemStack item) {

    public static final Codec<SandPaperItemComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(ItemStack.OPTIONAL_CODEC.fieldOf(
        "item").forGetter(SandPaperItemComponent::item)).apply(instance, SandPaperItemComponent::new));

    public static final PacketCodec<RegistryByteBuf, SandPaperItemComponent> STREAM_CODEC = PacketCodec.tuple(
        ItemStack.OPTIONAL_PACKET_CODEC,
        SandPaperItemComponent::item,
        SandPaperItemComponent::new
    );

    @Override
    public boolean equals(Object arg0) {
        return arg0 instanceof ItemStack otherItem && ItemStack.areItemsAndComponentsEqual(otherItem, item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item.getItem(), item.getCount(), item.getComponents());
    }

}
