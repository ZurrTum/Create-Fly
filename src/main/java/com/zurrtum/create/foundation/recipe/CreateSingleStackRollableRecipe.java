package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public interface CreateSingleStackRollableRecipe extends CreateRollableRecipe<SingleRecipeInput> {
    Ingredient ingredient();

    @Override
    default boolean matches(SingleRecipeInput input, Level world) {
        return ingredient().test(input.item());
    }

    @Override
    default ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        SequencedAssemblyJunk junk = input.item().get(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
        if (junk != null && junk.hasJunk()) {
            return junk.getJunk();
        }
        return CreateRollableRecipe.super.assemble(input, registries);
    }
}
