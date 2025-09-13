package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItemAttributeTypes;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.netty.buffer.ByteBuf;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AddedByAttribute(String modId) implements ItemAttribute {
    public static final MapCodec<AddedByAttribute> CODEC = Codec.STRING.xmap(AddedByAttribute::new, AddedByAttribute::modId).fieldOf("value");

    public static final PacketCodec<ByteBuf, AddedByAttribute> PACKET_CODEC = PacketCodecs.STRING.xmap(
        AddedByAttribute::new,
        AddedByAttribute::modId
    );

    private static String getCreatorModId(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).getNamespace();
    }

    @Override
    public boolean appliesTo(ItemStack stack, World world) {
        return modId.equals(getCreatorModId(stack));
    }

    @Override
    public String getTranslationKey() {
        return "added_by";
    }

    @Override
    public Object[] getTranslationParameters() {
        ModContainer container = FabricLoader.getInstance().getModContainer(modId).orElse(null);
        String name = container == null ? StringUtils.capitalize(modId) : container.getMetadata().getName();
        return new Object[]{name};
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.ADDED_BY;
    }

    public static class Type implements ItemAttributeType {
        @Override
        public @NotNull ItemAttribute createAttribute() {
            return new AddedByAttribute("dummy");
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
            return List.of(new AddedByAttribute(getCreatorModId(stack)));
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
