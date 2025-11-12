package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record SandPaperItemComponent(ItemStack item) {

    public static final Codec<SandPaperItemComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(ItemStack.OPTIONAL_CODEC.fieldOf(
        "item").forGetter(SandPaperItemComponent::item)).apply(instance, SandPaperItemComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SandPaperItemComponent> STREAM_CODEC = StreamCodec.composite(
        ItemStack.OPTIONAL_STREAM_CODEC,
        SandPaperItemComponent::item,
        SandPaperItemComponent::new
    );

    @Override
    public boolean equals(Object arg0) {
        return arg0 instanceof ItemStack otherItem && ItemStack.isSameItemSameComponents(otherItem, item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item.getItem(), item.getCount(), item.getComponents());
    }

}
