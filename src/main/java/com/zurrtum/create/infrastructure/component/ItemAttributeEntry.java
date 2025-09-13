package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record ItemAttributeEntry(ItemAttribute attribute, boolean inverted) {
    public static final Codec<ItemAttributeEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
        ItemAttribute.CODEC.fieldOf("attribute")
            .forGetter(ItemAttributeEntry::attribute),
        Codec.BOOL.fieldOf("inverted").forGetter(ItemAttributeEntry::inverted)
    ).apply(i, ItemAttributeEntry::new));

    public static final PacketCodec<RegistryByteBuf, ItemAttributeEntry> STREAM_CODEC = PacketCodec.tuple(
        ItemAttribute.PACKET_CODEC,
        ItemAttributeEntry::attribute,
        PacketCodecs.BOOLEAN,
        ItemAttributeEntry::inverted,
        ItemAttributeEntry::new
    );
}