package com.zurrtum.create.foundation.pack;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;

import java.util.Optional;

public class EmptyJsonOps extends RegistryOps<JsonElement> implements RegistryEntryOwner<Item> {
    public static final EmptyJsonOps INSTANCE = new EmptyJsonOps();

    private EmptyJsonOps() {
        super(JsonOps.INSTANCE, null);
    }

    @SuppressWarnings("deprecation")
    public static Ingredient ofTag(TagKey<Item> inputTag) {
        return Ingredient.ofTag(RegistryEntryList.of(EmptyJsonOps.INSTANCE, inputTag));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Optional<RegistryEntryOwner<E>> getOwner(RegistryKey<? extends Registry<? extends E>> registryRef) {
        return Optional.of((RegistryEntryOwner<E>) this);
    }
}
