package com.zurrtum.create.content.equipment.toolbox;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllRecipeSerializers;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ToolboxDyeingRecipe extends CustomRecipe {

    public ToolboxDyeingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int toolboxes = 0;
        int dyes = 0;

        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (Block.byItem(stack.getItem()) instanceof ToolboxBlock) {
                    ++toolboxes;
                } else {
                    if (!stack.is(AllItemTags.DYES))
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
    public ItemStack assemble(CraftingInput input) {
        ItemStack toolbox = ItemStack.EMPTY;
        DyeColor color = DyeColor.BROWN;

        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (Block.byItem(stack.getItem()) instanceof ToolboxBlock) {
                    toolbox = stack;
                } else {
                    DyeColor color1 = AllItemTags.getDyeColor(stack);
                    if (color1 != null) {
                        color = color1;
                    }
                }
            }
        }

        ItemStack dyedToolbox = ToolboxBlock.getColorBlock(color).asItem().getDefaultInstance();
        DataComponentPatch componentChanges = toolbox.getComponentsPatch();
        if (!componentChanges.isEmpty()) {
            dyedToolbox.applyComponents(componentChanges);
        }

        return dyedToolbox;
    }

    @Override
    public RecipeSerializer<ToolboxDyeingRecipe> getSerializer() {
        return AllRecipeSerializers.TOOLBOX_DYEING;
    }

}