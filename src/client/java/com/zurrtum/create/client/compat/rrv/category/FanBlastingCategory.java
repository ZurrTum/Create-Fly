package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.google.common.base.Suppliers;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.FanBlastingView;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class FanBlastingCategory extends CreateCategory {
    public static final FanBlastingCategory INSTANCE = new FanBlastingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        Collection<RecipeHolder<BlastingRecipe>> blastingRecipes = recipeManager.getRecipesForType(RecipeType.BLASTING);
        Collection<RecipeHolder<SmokingRecipe>> smokingRecipes = recipeManager.getRecipesForType(RecipeType.SMOKING);
        ClientLevel level = Minecraft.getInstance().level;
        Supplier<ContextMap> context = Suppliers.memoize(() -> SlotDisplayContext.fromLevel(level));
        for (RecipeHolder<BlastingRecipe> entry : blastingRecipes) {
            addRecipe(output, entry, level, null, smokingRecipes, context);
        }
        for (RecipeHolder<SmeltingRecipe> entry : recipeManager.getRecipesForType(RecipeType.SMELTING)) {
            addRecipe(output, entry, level, blastingRecipes, smokingRecipes, context);
        }
    }

    private static void addRecipe(
        List<ReliableClientRecipe> output,
        RecipeHolder<? extends SingleItemRecipe> entry,
        ClientLevel level,
        @Nullable Collection<RecipeHolder<BlastingRecipe>> blastingRecipes,
        Collection<RecipeHolder<SmokingRecipe>> smokingRecipes,
        Supplier<ContextMap> context
    ) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return;
        }
        SingleItemRecipe recipe = entry.value();
        Ingredient ingredient = recipe.input();
        CustomIngredient customIngredient = ((FabricIngredient) ingredient).getCustomIngredient();
        ItemStack firstInput;
        if (customIngredient == null) {
            firstInput = ingredient.values.stream().findFirst().map(item -> item.value().getDefaultInstance())
                .orElse(ItemStack.EMPTY);
        } else {
            firstInput = customIngredient.display().resolveForFirstStack(context.get());
        }
        if (firstInput.isEmpty()) {
            return;
        }
        SingleRecipeInput input = new SingleRecipeInput(firstInput);
        if (blastingRecipes != null && blastingRecipes.stream().filter(e -> e.value().matches(input, level)).findFirst()
            .filter(AllRecipeTypes.CAN_BE_AUTOMATED).isPresent()) {
            return;
        }
        if (smokingRecipes.stream().filter(e -> e.value().matches(input, level)).findFirst()
            .filter(AllRecipeTypes.CAN_BE_AUTOMATED).isPresent()) {
            return;
        }
        output.add(new FanBlastingView(entry.id().identifier(), recipe.result(), ingredient));
    }

    public FanBlastingCategory() {
        super("fan_blasting");
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
