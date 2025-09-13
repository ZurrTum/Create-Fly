package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItemAttributeTypes;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record ColorAttribute(DyeColor color) implements ItemAttribute {
    public static final MapCodec<ColorAttribute> CODEC = DyeColor.CODEC.xmap(ColorAttribute::new, ColorAttribute::color).fieldOf("value");

    public static final PacketCodec<ByteBuf, ColorAttribute> PACKET_CODEC = DyeColor.PACKET_CODEC.xmap(ColorAttribute::new, ColorAttribute::color);

    private static Collection<DyeColor> findMatchingDyeColors(ItemStack stack) {
        DyeColor color = AllItemTags.getDyeColor(stack);
        if (color != null)
            return Collections.singletonList(color);

        Set<DyeColor> colors = new HashSet<>();
        if (stack.contains(DataComponentTypes.FIREWORKS)) {
            if (stack.getItem() instanceof FireworkRocketItem || stack.isOf(Items.FIREWORK_STAR)) {
                List<FireworkExplosionComponent> explosions = stack.get(DataComponentTypes.FIREWORKS).explosions();
                for (FireworkExplosionComponent explosion : explosions) {
                    colors.addAll(getFireworkStarColors(explosion));
                }
            }
        }

        Arrays.stream(DyeColor.values()).filter(c -> Registries.ITEM.getId(stack.getItem()).getPath().startsWith(c.getId() + "_"))
            .forEach(colors::add);

        return colors;
    }

    private static Collection<DyeColor> getFireworkStarColors(FireworkExplosionComponent explosion) {
        Set<DyeColor> colors = new HashSet<>();
        Arrays.stream(explosion.colors().toIntArray()).mapToObj(DyeColor::byFireworkColor).forEach(colors::add);
        Arrays.stream(explosion.fadeColors().toIntArray()).mapToObj(DyeColor::byFireworkColor).forEach(colors::add);
        return colors;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack, World level) {
        return findMatchingDyeColors(itemStack).stream().anyMatch(color::equals);
    }

    @Override
    public String getTranslationKey() {
        return "color";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[]{Text.translatable("color.minecraft." + color.getId())};
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.HAS_COLOR;
    }

    public static class Type implements ItemAttributeType {
        @Override
        public @NotNull ItemAttribute createAttribute() {
            return new ColorAttribute(DyeColor.PURPLE);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
            List<ItemAttribute> list = new ArrayList<>();

            for (DyeColor color : ColorAttribute.findMatchingDyeColors(stack)) {
                list.add(new ColorAttribute(color));
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
