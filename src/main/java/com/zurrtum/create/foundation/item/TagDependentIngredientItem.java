package com.zurrtum.create.foundation.item;

import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class TagDependentIngredientItem extends Item {

    private final TagKey<Item> tag;

    public TagDependentIngredientItem(Properties properties, TagKey<Item> tag) {
        super(properties);
        this.tag = tag;
    }

    public static Function<Properties, TagDependentIngredientItem> tag(String path) {
        return settings -> new TagDependentIngredientItem(settings, TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path)));
    }

    public void addTo(CreativeModeTab.Output entries) {
        if (shouldHide()) {
            return;
        }
        entries.accept(this);
    }

    public boolean shouldHide() {
        for (Holder<Item> ignored : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            return false; // at least 1 present
        }
        return true; // none present
    }

}
