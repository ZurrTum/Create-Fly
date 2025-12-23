package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CrushingCategory extends CreateCategory {
    @Override
    public Component getDisplayName() {
        return CreateLang.translateDirect("recipe.crushing");
    }

    @Override
    public int getDisplayHeight() {
        return 98;
    }

    @Override
    public int getSlotCount() {
        return 6;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.CRUSHING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.CRUSHING_WHEEL.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return AllItems.CRUSHED_GOLD.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.CRUSHING_WHEEL.getDefaultInstance());
    }
}
