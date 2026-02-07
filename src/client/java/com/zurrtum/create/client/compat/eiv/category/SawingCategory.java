package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class SawingCategory extends CreateCategory {
    @Override
    public Text getDisplayName() {
        return CreateLang.translateDirect("recipe.sawing");
    }

    @Override
    public int getDisplayHeight() {
        return 62;
    }

    @Override
    public int getSlotCount() {
        return 5;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.SAWING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_SAW.getDefaultStack();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.OAK_LOG.getDefaultStack();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_SAW.getDefaultStack());
    }
}
