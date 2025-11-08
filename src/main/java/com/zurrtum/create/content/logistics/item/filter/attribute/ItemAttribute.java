package com.zurrtum.create.content.logistics.item.filter.attribute;

import com.mojang.serialization.Codec;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.catnip.codecs.CatnipCodecUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface ItemAttribute {
    Codec<ItemAttribute> CODEC = CreateRegistries.ITEM_ATTRIBUTE_TYPE.getCodec().dispatch(ItemAttribute::getType, ItemAttributeType::codec);
    PacketCodec<RegistryByteBuf, ItemAttribute> PACKET_CODEC = PacketCodecs.registryValue(CreateRegistryKeys.ITEM_ATTRIBUTE_TYPE)
        .dispatch(ItemAttribute::getType, ItemAttributeType::packetCodec);

    static NbtCompound saveStatic(ItemAttribute attribute, RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        nbt.put("attribute", CatnipCodecUtils.encode(CODEC, registries, attribute).orElseThrow());
        return nbt;
    }

    @Nullable
    static ItemAttribute loadStatic(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        return CatnipCodecUtils.decodeOrNull(CODEC, registries, nbt.get("attribute"));
    }

    static List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
        List<ItemAttribute> attributes = new ArrayList<>();
        for (ItemAttributeType type : CreateRegistries.ITEM_ATTRIBUTE_TYPE) {
            attributes.addAll(type.getAllAttributes(stack, level));
        }
        return attributes;
    }

    boolean appliesTo(ItemStack stack, World world);

    ItemAttributeType getType();

    default MutableText format(boolean inverted) {
        return Text.translatable("create.item_attributes." + getTranslationKey() + (inverted ? ".inverted" : ""), getTranslationParameters());
    }

    String getTranslationKey();

    default Object[] getTranslationParameters() {
        return new String[0];
    }
}