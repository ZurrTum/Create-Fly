package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItemAttributeTypes;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record EnchantAttribute(@Nullable RegistryEntry<Enchantment> enchantment) implements ItemAttribute {
    public static final MapCodec<EnchantAttribute> CODEC = Enchantment.ENTRY_CODEC.xmap(EnchantAttribute::new, EnchantAttribute::enchantment)
        .fieldOf("value");

    public static final PacketCodec<RegistryByteBuf, EnchantAttribute> PACKET_CODEC = Enchantment.ENTRY_PACKET_CODEC.xmap(
        EnchantAttribute::new,
        EnchantAttribute::enchantment
    );

    @Override
    public boolean appliesTo(ItemStack itemStack, World level) {
        return EnchantmentHelper.getEnchantments(itemStack).getEnchantments().contains(enchantment);
    }

    @Override
    public String getTranslationKey() {
        return "has_enchant";
    }

    @Override
    public Object[] getTranslationParameters() {
        String parameter = "";
        if (enchantment != null)
            parameter = enchantment.value().description().getString();
        return new Object[]{parameter};
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.HAS_ENCHANT;
    }

    public static class Type implements ItemAttributeType {
        @Override
        public @NotNull ItemAttribute createAttribute() {
            return new EnchantAttribute(null);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
            List<ItemAttribute> list = new ArrayList<>();

            for (RegistryEntry<Enchantment> enchantmentHolder : EnchantmentHelper.getEnchantments(stack).getEnchantments()) {
                list.add(new EnchantAttribute(enchantmentHolder));
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
