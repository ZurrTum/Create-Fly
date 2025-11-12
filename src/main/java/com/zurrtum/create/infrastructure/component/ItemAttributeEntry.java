package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ItemAttributeEntry(ItemAttribute attribute, boolean inverted) {
    public static final Codec<ItemAttributeEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
        ItemAttribute.CODEC.fieldOf("attribute")
            .forGetter(ItemAttributeEntry::attribute),
        Codec.BOOL.fieldOf("inverted").forGetter(ItemAttributeEntry::inverted)
    ).apply(i, ItemAttributeEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeEntry> STREAM_CODEC = StreamCodec.composite(
        ItemAttribute.PACKET_CODEC,
        ItemAttributeEntry::attribute,
        ByteBufCodecs.BOOL,
        ItemAttributeEntry::inverted,
        ItemAttributeEntry::new
    );
}