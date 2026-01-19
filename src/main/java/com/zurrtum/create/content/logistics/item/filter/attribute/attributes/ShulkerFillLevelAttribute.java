package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItemAttributeTypes;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public record ShulkerFillLevelAttribute(@Nullable ShulkerLevels levels) implements ItemAttribute {
    public static final MapCodec<ShulkerFillLevelAttribute> CODEC = ShulkerLevels.CODEC.xmap(
        ShulkerFillLevelAttribute::new,
        ShulkerFillLevelAttribute::levels
    ).fieldOf("value");

    public static final StreamCodec<ByteBuf, ShulkerFillLevelAttribute> PACKET_CODEC = ShulkerLevels.STREAM_CODEC.map(
        ShulkerFillLevelAttribute::new,
        ShulkerFillLevelAttribute::levels
    );

    @Override
    public boolean appliesTo(ItemStack stack, Level level) {
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
            parameter = Component.translatable("create.item_attributes." + getTranslationKey() + "." + levels.key).getString();
        return new Object[]{parameter};
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.SHULKER_FILL_LEVEL;
    }

    enum ShulkerLevels implements StringRepresentable {
        EMPTY("empty", amount -> amount == 0),
        PARTIAL("partial", amount -> amount > 0 && amount < ShulkerBoxBlockEntity.CONTAINER_SIZE),
        FULL("full", amount -> amount == ShulkerBoxBlockEntity.CONTAINER_SIZE);

        public static final Codec<ShulkerLevels> CODEC = StringRepresentable.fromEnum(ShulkerLevels::values);
        public static final StreamCodec<ByteBuf, ShulkerLevels> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ShulkerLevels.class);

        private final Predicate<Integer> requiredSize;
        private final String key;

        ShulkerLevels(String key, Predicate<Integer> requiredSize) {
            this.key = key;
            this.requiredSize = requiredSize;
        }

        @Nullable
        public static ShulkerLevels fromKey(String key) {
            return Arrays.stream(values()).filter(shulkerLevels -> shulkerLevels.key.equals(key)).findFirst().orElse(null);
        }

        private static boolean isShulker(ItemStack stack) {
            return Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public boolean canApply(ItemStack testStack) {
            if (!isShulker(testStack))
                return false;
            ItemContainerContents contents = testStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            if (contents == ItemContainerContents.EMPTY)
                return requiredSize.test(0);
            if (testStack.has(DataComponents.CONTAINER_LOOT))
                return false;
            int size = 0;
            for (ItemStackTemplate _ : contents.nonEmptyItems()) {
                size++;
            }
            return requiredSize.test(size);
        }
    }

    public static class Type implements ItemAttributeType {
        @Override
        public ItemAttribute createAttribute() {
            return new ShulkerFillLevelAttribute(null);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
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
        public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
