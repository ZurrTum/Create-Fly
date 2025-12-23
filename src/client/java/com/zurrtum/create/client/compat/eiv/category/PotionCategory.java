package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class PotionCategory extends CreateCategory {
    @Override
    public Component getDisplayName() {
        return CreateLang.translateDirect("recipe.automatic_brewing");
    }

    @Override
    public int getDisplayHeight() {
        return 99;
    }

    @Override
    public int getSlotCount() {
        return 3;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.AUTOMATIC_BREWING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.MECHANICAL_MIXER.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.BREWING_STAND.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.MECHANICAL_MIXER.getDefaultInstance(), AllItems.BASIN.getDefaultInstance());
    }
}
