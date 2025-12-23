package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class MysteriousItemConversionCategory extends CreateCategory {
    @Override
    public Component getDisplayName() {
        return CreateLang.translateDirect("recipe.mystery_conversion");
    }

    @Override
    public int getDisplayHeight() {
        return 29;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.MYSTERY_CONVERSION.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.PECULIAR_BELL.getDefaultInstance();
    }
}
