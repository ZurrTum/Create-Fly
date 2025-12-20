package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class MixingCategory extends CreateCategory {
    @Override
    public Text getDisplayName() {
        return CreateLang.translateDirect("recipe.mixing");
    }

    @Override
    public int getDisplayHeight() {
        return 99;
    }

    @Override
    public int getSlotCount() {
        return 9;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.MIXING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_MIXER.getDefaultStack();
    }

    @Override
    public ItemStack getSubIcon() {
        return AllItems.BASIN.getDefaultStack();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_MIXER.getDefaultStack(), AllItems.BASIN.getDefaultStack());
    }
}
