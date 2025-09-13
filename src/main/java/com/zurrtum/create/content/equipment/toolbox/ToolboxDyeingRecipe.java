package com.zurrtum.create.content.equipment.toolbox;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllRecipeSerializers;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;

public class ToolboxDyeingRecipe extends SpecialCraftingRecipe {

    public ToolboxDyeingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World level) {
        int toolboxes = 0;
        int dyes = 0;

        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (Block.getBlockFromItem(stack.getItem()) instanceof ToolboxBlock) {
                    ++toolboxes;
                } else {
                    if (!stack.isIn(AllItemTags.DYES))
                        return false;
                    ++dyes;
                }

                if (dyes > 1 || toolboxes > 1) {
                    return false;
                }
            }
        }

        return toolboxes == 1 && dyes == 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack toolbox = ItemStack.EMPTY;
        DyeColor color = DyeColor.BROWN;

        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (Block.getBlockFromItem(stack.getItem()) instanceof ToolboxBlock) {
                    toolbox = stack;
                } else {
                    DyeColor color1 = AllItemTags.getDyeColor(stack);
                    if (color1 != null) {
                        color = color1;
                    }
                }
            }
        }

        ItemStack dyedToolbox = ToolboxBlock.getColorBlock(color).asItem().getDefaultStack();
        ComponentChanges componentChanges = toolbox.getComponentChanges();
        if (!componentChanges.isEmpty()) {
            dyedToolbox.applyUnvalidatedChanges(componentChanges);
        }

        return dyedToolbox;
    }

    @Override
    public RecipeSerializer<ToolboxDyeingRecipe> getSerializer() {
        return AllRecipeSerializers.TOOLBOX_DYEING;
    }

}