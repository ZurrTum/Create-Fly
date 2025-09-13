package com.zurrtum.create.foundation.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class TagDependentIngredientItem extends Item {

    private final TagKey<Item> tag;

    public TagDependentIngredientItem(Settings properties, TagKey<Item> tag) {
        super(properties);
        this.tag = tag;
    }

    public static Function<Settings, TagDependentIngredientItem> tag(String path) {
        return settings -> new TagDependentIngredientItem(settings, TagKey.of(RegistryKeys.ITEM, Identifier.of("c", path)));
    }

    public void addTo(ItemGroup.Entries entries) {
        if (shouldHide()) {
            return;
        }
        entries.add(this);
    }

    public boolean shouldHide() {
        for (RegistryEntry<Item> ignored : Registries.ITEM.iterateEntries(tag)) {
            return false; // at least 1 present
        }
        return true; // none present
    }

}
