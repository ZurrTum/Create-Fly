package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItemAttributeTypes;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ItemNameAttribute(String itemName) implements ItemAttribute {
    public static final MapCodec<ItemNameAttribute> CODEC = Codec.STRING.xmap(ItemNameAttribute::new, ItemNameAttribute::itemName).fieldOf("value");

    public static final StreamCodec<ByteBuf, ItemNameAttribute> PACKET_CODEC = ByteBufCodecs.STRING_UTF8.map(
        ItemNameAttribute::new,
        ItemNameAttribute::itemName
    );

    private static String extractCustomName(ItemStack stack, Level level) {
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            try {
                String customName = stack.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty()).getString();
                Optional<Component> component = ComponentSerialization.CODEC.parse(
                    level.registryAccess()
                        .createSerializationContext(JsonOps.INSTANCE),
                    JsonParser.parseString(customName.isEmpty() ? "\"\"" : customName)
                ).result();
                if (component.isPresent()) {
                    return component.get().getString();
                }
            } catch (JsonParseException ignored) {
            }
        }
        return "";
    }

    @Override
    public boolean appliesTo(ItemStack itemStack, Level level) {
        return extractCustomName(itemStack, level).equals(itemName);
    }

    @Override
    public String getTranslationKey() {
        return "has_name";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[]{itemName};
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.HAS_NAME;
    }

    public static class Type implements ItemAttributeType {
        @Override
        public ItemAttribute createAttribute() {
            return new ItemNameAttribute("dummy");
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
            List<ItemAttribute> list = new ArrayList<>();

            String name = extractCustomName(stack, level);
            if (!name.isEmpty()) {
                list.add(new ItemNameAttribute(name));
            }

            return list;
        }

        @Override
        public MapCodec<? extends ItemAttribute> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
