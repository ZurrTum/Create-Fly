package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SandPaperPolishingCategory extends CreateCategory {
    @Override
    public Component getDisplayName() {
        return CreateLang.translateDirect("recipe.sandpaper_polishing");
    }

    @Override
    public int getDisplayHeight() {
        return 48;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.SANDPAPER_POLISHING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.SAND_PAPER.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.SAND_PAPER.getDefaultInstance(), AllItems.RED_SAND_PAPER.getDefaultInstance());
    }
}
