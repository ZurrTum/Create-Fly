package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.mojang.serialization.Codec;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record ItemNameAttribute(String itemName) implements ItemAttribute {
    public static final MapCodec<ItemNameAttribute> CODEC = Codec.STRING.xmap(ItemNameAttribute::new, ItemNameAttribute::itemName).fieldOf("value");

    public static final PacketCodec<ByteBuf, ItemNameAttribute> PACKET_CODEC = PacketCodecs.STRING.xmap(
        ItemNameAttribute::new,
        ItemNameAttribute::itemName
    );

    private static String extractCustomName(ItemStack stack) {
        Text text = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (text != null) {
            return text.getString();
        }
        return "";
    }

    @Override
    public boolean appliesTo(ItemStack itemStack, World level) {
        return extractCustomName(itemStack).equals(itemName);
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

            String name = extractCustomName(stack);
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
