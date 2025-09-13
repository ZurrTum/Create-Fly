package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItemAttributeTypes;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public record ShulkerFillLevelAttribute(ShulkerLevels levels) implements ItemAttribute {
    public static final MapCodec<ShulkerFillLevelAttribute> CODEC = ShulkerLevels.CODEC.xmap(
        ShulkerFillLevelAttribute::new,
        ShulkerFillLevelAttribute::levels
    ).fieldOf("value");

    public static final PacketCodec<ByteBuf, ShulkerFillLevelAttribute> PACKET_CODEC = ShulkerLevels.STREAM_CODEC.xmap(
        ShulkerFillLevelAttribute::new,
        ShulkerFillLevelAttribute::levels
    );

    @Override
    public boolean appliesTo(ItemStack stack, World level) {
        return levels != null && levels.canApply(stack);
    }

    @Override
    public String getTranslationKey() {
        return "shulker_level";
    }

    @Override
    public Object[] getTranslationParameters() {
        String parameter = "";
        if (levels != null)
            parameter = Text.translatable("create.item_attributes." + getTranslationKey() + "." + levels.key).getString();
        return new Object[]{parameter};
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.SHULKER_FILL_LEVEL;
    }

    enum ShulkerLevels implements StringIdentifiable {
        EMPTY("empty", amount -> amount == 0),
        PARTIAL("partial", amount -> amount > 0 && amount < Integer.MAX_VALUE),
        FULL("full", amount -> amount == Integer.MAX_VALUE);

        public static final Codec<ShulkerLevels> CODEC = StringIdentifiable.createCodec(ShulkerLevels::values);
        public static final PacketCodec<ByteBuf, ShulkerLevels> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ShulkerLevels.class);

        private final Predicate<Integer> requiredSize;
        private final String key;

        ShulkerLevels(String key, Predicate<Integer> requiredSize) {
            this.key = key;
            this.requiredSize = requiredSize;
        }

        @Nullable
        public static ShulkerFillLevelAttribute.ShulkerLevels fromKey(String key) {
            return Arrays.stream(values()).filter(shulkerLevels -> shulkerLevels.key.equals(key)).findFirst().orElse(null);
        }

        private static boolean isShulker(ItemStack stack) {
            return Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock;
        }

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }

        public boolean canApply(ItemStack testStack) {
            if (!isShulker(testStack))
                return false;
            ContainerComponent contents = testStack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
            if (contents == ContainerComponent.DEFAULT)
                return requiredSize.test(0);
            if (testStack.contains(DataComponentTypes.CONTAINER_LOOT))
                return false;
            if (!contents.stacks.isEmpty()) {
                int rawSize = contents.stacks.size();
                if (rawSize < 27)
                    return requiredSize.test(rawSize);

                DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
                contents.copyTo(inventory);
                boolean isFull = inventory.stream().allMatch(itemStack -> !itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxCount());
                return requiredSize.test(isFull ? Integer.MAX_VALUE : rawSize);
            }
            return requiredSize.test(0);
        }
    }

    public static class Type implements ItemAttributeType {
        @Override
        public @NotNull ItemAttribute createAttribute() {
            return new ShulkerFillLevelAttribute(null);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
            List<ItemAttribute> list = new ArrayList<>();

            for (ShulkerLevels shulkerLevels : ShulkerLevels.values()) {
                if (shulkerLevels.canApply(stack)) {
                    list.add(new ShulkerFillLevelAttribute(shulkerLevels));
                }
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
