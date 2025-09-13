package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public interface CreateSingleStackRollableRecipe extends CreateRollableRecipe<SingleStackRecipeInput> {
    Ingredient ingredient();

    @Override
    default boolean matches(SingleStackRecipeInput input, World world) {
        return ingredient().test(input.item());
    }

    @Override
    default ItemStack craft(SingleStackRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        SequencedAssemblyJunk junk = input.item().get(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
        if (junk != null && junk.hasJunk()) {
            return junk.getJunk();
        }
        return CreateRollableRecipe.super.craft(input, registries);
    }
}
