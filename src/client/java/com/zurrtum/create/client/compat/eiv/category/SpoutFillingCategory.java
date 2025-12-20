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

public class SpoutFillingCategory extends CreateCategory {
    @Override
    public Text getDisplayName() {
        return CreateLang.translateDirect("recipe.spout_filling");
    }

    @Override
    public int getDisplayHeight() {
        return 66;
    }

    @Override
    public int getSlotCount() {
        return 3;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.SPOUT_FILLING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.SPOUT.getDefaultStack();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.WATER_BUCKET.getDefaultStack();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.SPOUT.getDefaultStack());
    }
}
