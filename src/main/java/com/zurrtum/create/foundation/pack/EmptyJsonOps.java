package com.zurrtum.create.foundation.pack;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

public class EmptyJsonOps extends RegistryOps<JsonElement> implements HolderGetter<Item> {
    public static final EmptyJsonOps INSTANCE = new EmptyJsonOps();

    private EmptyJsonOps() {
        super(JsonOps.INSTANCE, null);
    }

    @SuppressWarnings("deprecation")
    public static Ingredient ofTag(TagKey<Item> inputTag) {
        return Ingredient.of(HolderSet.emptyNamed(INSTANCE, inputTag));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Optional<HolderGetter<E>> getter(final ResourceKey<? extends Registry<? extends E>> registryKey) {
        return Optional.of((HolderGetter<E>) this);
    }

    @Override
    public Optional<Reference<Item>> get(ResourceKey<Item> id) {
        return Optional.empty();
    }

    @Override
    public Optional<Named<Item>> get(TagKey<Item> id) {
        return Optional.empty();
    }
}
