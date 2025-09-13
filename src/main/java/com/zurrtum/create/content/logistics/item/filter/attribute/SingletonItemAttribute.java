package com.zurrtum.create.content.logistics.item.filter.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class SingletonItemAttribute implements ItemAttribute {
    private final Type type;
    private final BiPredicate<ItemStack, World> predicate;
    private final String translationKey;

    public SingletonItemAttribute(Type type, BiPredicate<ItemStack, World> predicate, String translationKey) {
        this.type = type;
        this.predicate = predicate;
        this.translationKey = translationKey;
    }

    @Override
    public boolean appliesTo(ItemStack stack, World world) {
        return predicate.test(stack, world);
    }

    @Override
    public ItemAttributeType getType() {
        return type;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    public static final class Type implements ItemAttributeType {
        private final SingletonItemAttribute attribute;

        public Type(Function<Type, SingletonItemAttribute> singletonFunc) {
            this.attribute = singletonFunc.apply(this);
        }

        @Override
        public @NotNull ItemAttribute createAttribute() {
            return attribute;
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
            if (attribute.appliesTo(stack, level)) {
                return List.of(attribute);
            }
            return List.of();
        }

        @Override
        public MapCodec<? extends ItemAttribute> codec() {
            return Codec.unit(attribute).fieldOf("value");
        }

        @Override
        public PacketCodec<? super RegistryByteBuf, ? extends ItemAttribute> packetCodec() {
            return PacketCodec.unit(attribute);
        }
    }
}
