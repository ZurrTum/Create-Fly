package com.zurrtum.create.foundation.pack;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public class EmptyJsonOps extends RegistryOps<JsonElement> implements HolderOwner<Item> {
    public static final EmptyJsonOps INSTANCE = new EmptyJsonOps();

    private EmptyJsonOps() {
        super(JsonOps.INSTANCE, null);
    }

    @SuppressWarnings("deprecation")
    public static Ingredient ofTag(TagKey<Item> inputTag) {
        return Ingredient.of(HolderSet.emptyNamed(EmptyJsonOps.INSTANCE, inputTag));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Optional<HolderOwner<E>> owner(ResourceKey<? extends Registry<? extends E>> registryRef) {
        return Optional.of((HolderOwner<E>) this);
    }
}
