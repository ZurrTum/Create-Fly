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

public class FanBlastingCategory extends CreateCategory {
    @Override
    public Component getDisplayName() {
        return CreateLang.translateDirect("recipe.fan_blasting");
    }

    @Override
    public int getDisplayHeight() {
        return 61;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.FAN_BLASTING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.PROPELLER.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.LAVA_BUCKET.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.ENCASED_FAN.getDefaultInstance());
    }
}
