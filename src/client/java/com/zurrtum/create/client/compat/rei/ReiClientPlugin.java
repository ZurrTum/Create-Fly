package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class ReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(
            new AutoCompactingCategory(), config -> {
                config.addWorkstations(EntryStacks.of(AllItems.MECHANICAL_PRESS.getDefaultStack()), EntryStacks.of(AllItems.BASIN));
                config.setPlusButtonArea(bounds -> new Rectangle(bounds.getMaxX() - 16, bounds.getMaxY() - 16, 10, 10));
            }
        );
    }
}
