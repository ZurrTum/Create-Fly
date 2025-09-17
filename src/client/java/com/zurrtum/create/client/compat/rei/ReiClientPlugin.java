package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry.CategoryConfiguration;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public class ReiClientPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<MysteriousItemConversionDisplay> MYSTERY_CONVERSION = CategoryIdentifier.of(MOD_ID, "mystery_conversion");

    @SuppressWarnings("unchecked")
    private <T extends Display> Consumer<CategoryConfiguration<T>> config(ItemConvertible... item) {
        EntryStack<ItemStack>[] workstations = new EntryStack[item.length];
        for (int i = 0; i < item.length; i++) {
            workstations[i] = EntryStacks.of(item[i]);
        }
        return config -> {
            if (workstations.length > 0) {
                config.addWorkstations(workstations);
            }
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
        registry.add(new CrushingCategory(), config(AllItems.CRUSHING_WHEEL));
        registry.add(new MysteriousItemConversionCategory(), config());
        registry.add(new ManualApplicationCategory(), config());
        registry.add(new DeployingCategory(), config(AllItems.DEPLOYER, AllItems.DEPOT, AllItems.BELT_CONNECTOR));
        registry.add(new DrainingCategory(), config(AllItems.ITEM_DRAIN));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.add(new MysteriousItemConversionDisplay(AllItems.EMPTY_BLAZE_BURNER, AllItems.BLAZE_BURNER));
        registry.add(new MysteriousItemConversionDisplay(AllItems.PECULIAR_BELL, AllItems.HAUNTED_BELL));
    }
}
