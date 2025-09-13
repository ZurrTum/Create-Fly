package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

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
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record BookCopyAttribute(int generation) implements ItemAttribute {
    public static final MapCodec<BookCopyAttribute> CODEC = Codecs.NON_NEGATIVE_INT.xmap(BookCopyAttribute::new, BookCopyAttribute::generation)
        .fieldOf("value");

    public static final PacketCodec<ByteBuf, BookCopyAttribute> PACKET_CODEC = PacketCodecs.INTEGER.xmap(
        BookCopyAttribute::new,
        BookCopyAttribute::generation
    );

    private static int extractGeneration(ItemStack stack) {
        if (stack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
            return stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).generation();
        }

        return -1;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack, World level) {
        return extractGeneration(itemStack) == generation;
    }

    @Override
    public String getTranslationKey() {
        return switch (generation) {
            case 0 -> "book_copy_original";
            case 1 -> "book_copy_first";
            case 2 -> "book_copy_second";
            default -> "book_copy_tattered";
        };
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.BOOK_COPY;
    }

    public static class Type implements ItemAttributeType {
        @Override
        public @NotNull ItemAttribute createAttribute() {
            return new BookCopyAttribute(-1);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
            List<ItemAttribute> list = new ArrayList<>();

            int generation = BookCopyAttribute.extractGeneration(stack);
            if (generation >= 0) {
                list.add(new BookCopyAttribute(generation));
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
