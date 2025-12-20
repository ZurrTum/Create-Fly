package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class SandPaperPolishingCategory extends CreateCategory {
    @Override
    public Text getDisplayName() {
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
        return AllItems.SAND_PAPER.getDefaultStack();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.SAND_PAPER.getDefaultStack(), AllItems.RED_SAND_PAPER.getDefaultStack());
    }
}
