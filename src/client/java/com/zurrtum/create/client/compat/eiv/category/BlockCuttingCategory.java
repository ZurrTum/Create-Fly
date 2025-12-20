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

public class BlockCuttingCategory extends CreateCategory {
    @Override
    public Text getDisplayName() {
        return CreateLang.translateDirect("recipe.block_cutting");
    }

    @Override
    public int getDisplayHeight() {
        return 61;
    }

    @Override
    public int getSlotCount() {
        return 16;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.BLOCK_CUTTING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_SAW.getDefaultStack();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.STONE_BRICK_STAIRS.getDefaultStack();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_SAW.getDefaultStack());
    }
}
