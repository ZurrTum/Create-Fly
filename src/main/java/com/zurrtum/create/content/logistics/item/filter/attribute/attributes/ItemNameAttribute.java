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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ItemNameAttribute(String itemName) implements ItemAttribute {
    public static final MapCodec<ItemNameAttribute> CODEC = Codec.STRING.xmap(ItemNameAttribute::new, ItemNameAttribute::itemName).fieldOf("value");

    public static final PacketCodec<ByteBuf, ItemNameAttribute> PACKET_CODEC = PacketCodecs.STRING.xmap(
        ItemNameAttribute::new,
        ItemNameAttribute::itemName
    );

    private static String extractCustomName(ItemStack stack, World level) {
        if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            try {
                String customName = stack.getOrDefault(DataComponentTypes.CUSTOM_NAME, Text.empty()).getString();
                Optional<Text> component = TextCodecs.CODEC.parse(
                    level.getRegistryManager().getOps(JsonOps.INSTANCE),
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
    public boolean appliesTo(ItemStack itemStack, World level) {
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
        public @NotNull ItemAttribute createAttribute() {
            return new ItemNameAttribute("dummy");
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
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
        public PacketCodec<? super RegistryByteBuf, ? extends ItemAttribute> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
