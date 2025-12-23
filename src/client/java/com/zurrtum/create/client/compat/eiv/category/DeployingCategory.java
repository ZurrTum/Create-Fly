package com.zurrtum.create.client.compat.eiv.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateCategory;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DeployingCategory extends CreateCategory {
    @Override
    public Component getDisplayName() {
        return CreateLang.translateDirect("recipe.deploying");
    }

    @Override
    public int getDisplayHeight() {
        return 70;
    }

    @Override
    public int getSlotCount() {
        return 3;
    }

    @Override
    public Identifier getId() {
        return EivCommonPlugin.DEPLOYING.getId();
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.DEPLOYER.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.DEPLOYER.getDefaultInstance(), AllItems.DEPOT.getDefaultInstance(), AllItems.BELT_CONNECTOR.getDefaultInstance());
    }
}
