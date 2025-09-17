package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry.CategoryConfiguration;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public class ReiClientPlugin implements REIClientPlugin {
    @SuppressWarnings("unchecked")
    private <T extends Display> Consumer<CategoryConfiguration<T>> config(ItemConvertible... item) {
        EntryStack<ItemStack>[] workstations = new EntryStack[item.length];
        for (int i = 0; i < item.length; i++) {
            workstations[i] = EntryStacks.of(item[i]);
        }
        return config -> {
            config.addWorkstations(workstations);
            config.setPlusButtonArea(bounds -> new Rectangle(bounds.getMaxX() - 16, bounds.getMinY() + 6, 10, 10));
        };
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new AutoCompactingCategory(), config(AllItems.MECHANICAL_PRESS, AllItems.BASIN));
        registry.add(new CompactingCategory(), config(AllItems.MECHANICAL_PRESS, AllItems.BASIN));
        registry.add(new PressingCategory(), config(AllItems.MECHANICAL_PRESS));
        registry.add(new AutoMixingCategory(), config(AllItems.MECHANICAL_MIXER, AllItems.BASIN));
        registry.add(new MixingCategory(), config(AllItems.MECHANICAL_MIXER, AllItems.BASIN));
        registry.add(new MillingCategory(), config(AllItems.MILLSTONE));
        registry.add(new SawingCategory(), config(AllItems.MECHANICAL_SAW));
    }
}
