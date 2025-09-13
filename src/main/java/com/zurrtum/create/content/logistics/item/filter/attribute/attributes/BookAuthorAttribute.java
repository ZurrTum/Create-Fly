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
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record BookAuthorAttribute(String author) implements ItemAttribute {
    public static final MapCodec<BookAuthorAttribute> CODEC = Codec.STRING.xmap(BookAuthorAttribute::new, BookAuthorAttribute::author)
        .fieldOf("value");

    public static final PacketCodec<ByteBuf, BookAuthorAttribute> PACKET_CODEC = PacketCodecs.STRING.xmap(
        BookAuthorAttribute::new,
        BookAuthorAttribute::author
    );

    private static String extractAuthor(ItemStack stack) {
        if (stack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
            return stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).author();
        }

        return "";
    }

    @Override
    public boolean appliesTo(ItemStack itemStack, World level) {
        return extractAuthor(itemStack).equals(author);
    }

    @Override
    public String getTranslationKey() {
        return "book_author";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[]{author};
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.BOOK_AUTHOR;
    }

    public static class Type implements ItemAttributeType {
        @Override
        public @NotNull ItemAttribute createAttribute() {
            return new BookAuthorAttribute("dummy");
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
            List<ItemAttribute> list = new ArrayList<>();

            String name = BookAuthorAttribute.extractAuthor(stack);
            if (!name.isEmpty()) {
                list.add(new BookAuthorAttribute(name));
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
