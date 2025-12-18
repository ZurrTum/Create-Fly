package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class MillingCategory extends CreateCategory {
    @Override
    public Text getDisplayName() {
        return CreateLang.translateDirect("recipe.milling");
    }

    @Override
    public int getDisplayHeight() {
        return 47;
    }

    @Override
    public int getSlotCount() {
        return 4;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.MILLING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MILLSTONE.getDefaultStack();
    }

    @Override
    public ItemStack getSubIcon() {
        return AllItems.WHEAT_FLOUR.getDefaultStack();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MILLSTONE.getDefaultStack());
    }
}
