package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MechanicalCraftingCategory extends CreateCategory {
    @Override
    public Component getDisplayName() {
        return CreateLang.translateDirect("recipe.mechanical_crafting");
    }

    @Override
    public int getDisplayHeight() {
        return 94;
    }

    @Override
    public int getSlotCount() {
        return 26;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.MECHANICAL_CRAFTING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_CRAFTER.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_CRAFTER.getDefaultInstance());
    }
}
