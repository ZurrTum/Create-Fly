package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItemAttributeTypes;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public record InTagAttribute(TagKey<Item> tag) implements ItemAttribute {
    public static final MapCodec<InTagAttribute> CODEC = TagKey.codec(RegistryKeys.ITEM).xmap(InTagAttribute::new, InTagAttribute::tag)
        .fieldOf("value");

    public static final PacketCodec<ByteBuf, InTagAttribute> PACKET_CODEC = TagKey.packetCodec(RegistryKeys.ITEM)
        .xmap(InTagAttribute::new, InTagAttribute::tag);

    @Override
    public boolean appliesTo(ItemStack stack, World level) {
        return stack.isIn(tag);
    }

    @Override
    public String getTranslationKey() {
        return "in_tag";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[]{"#" + tag.id()};
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.IN_TAG;
    }

    public static class Type implements ItemAttributeType {
        @Override
        public @NotNull ItemAttribute createAttribute() {
            return new InTagAttribute(ItemTags.LOGS);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
            return stack.streamTags().map(InTagAttribute::new).collect(Collectors.toList());
        }

        @Override
        public MapCodec<? extends ItemAttribute> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<? super RegistryByteBuf, ? extends ItemAttribute> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
