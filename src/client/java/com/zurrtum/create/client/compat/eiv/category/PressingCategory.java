package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PressingCategory extends CreateCategory {
    @Override
    public Text getDisplayName() {
        return CreateLang.translateDirect("recipe.pressing");
    }

    @Override
    public int getDisplayHeight() {
        return 72;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.PRESSING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_PRESS.getDefaultStack();
    }

    @Override
    public @Nullable ItemStack getSubIcon() {
        return AllItems.IRON_SHEET.getDefaultStack();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_PRESS.getDefaultStack());
    }
}
