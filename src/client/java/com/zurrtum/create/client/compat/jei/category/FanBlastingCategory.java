package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FanBlastingCategory extends CreateCategory<RecipeEntry<? extends SingleStackRecipe>> {
    public static List<RecipeEntry<? extends SingleStackRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        List<RecipeEntry<? extends SingleStackRecipe>> recipes = new ArrayList<>();
        for (RecipeEntry<BlastingRecipe> entry : preparedRecipes.getAll(RecipeType.BLASTING)) {
            addRecipe(recipes, entry);
        }
        for (RecipeEntry<SmeltingRecipe> entry : preparedRecipes.getAll(RecipeType.SMELTING)) {
            addRecipe(recipes, entry);
        }
        return recipes;
    }

    private static void addRecipe(List<RecipeEntry<? extends SingleStackRecipe>> list, RecipeEntry<? extends SingleStackRecipe> entry) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return;
        }
        SingleStackRecipe recipe = entry.value();
        Ingredient ingredient = recipe.ingredient();
        Optional<ItemStack> firstInput = ingredient.entries.stream().findFirst().map(item -> item.value().getDefaultStack());
        if (firstInput.isEmpty()) {
            return;
        }
        SingleStackRecipeInput input = new SingleStackRecipeInput(firstInput.get());
        MinecraftServer server = Create.SERVER;
        ServerWorld world = server.getWorld(World.OVERWORLD);
        ServerRecipeManager recipeManager = server.getRecipeManager();
        if (recipe instanceof SmeltingRecipe) {
            Optional<RecipeEntry<BlastingRecipe>> blastingRecipe = recipeManager.getFirstMatch(RecipeType.BLASTING, input, world)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
            if (blastingRecipe.isPresent()) {
                return;
            }
        }
        Optional<RecipeEntry<SmokingRecipe>> smokingRecipe = recipeManager.getFirstMatch(RecipeType.SMOKING, input, world)
            .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
        if (smokingRecipe.isPresent()) {
            return;
        }
        list.add(entry);
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<? extends SingleStackRecipe>> getRecipeType() {
        return JeiClientPlugin.FAN_BLASTING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.fan_blasting");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.PROPELLER, Items.LAVA_BUCKET);
    }

    @Override
    public int getHeight() {
        return 72;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<? extends SingleStackRecipe> entry, IFocusGroup focuses) {
        SingleStackRecipe recipe = entry.value();
        builder.addInputSlot(21, 48).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        builder.addOutputSlot(141, 48).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(
        RecipeEntry<? extends SingleStackRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        DrawContext graphics,
        double mouseX,
        double mouseY
    ) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 46, 27);
        AllGuiTextures.JEI_LIGHT.render(graphics, 65, 39);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 54, 51);
        graphics.state.addSpecialElement(new FanRenderState(
            new Matrix3x2f(graphics.getMatrices()),
            56,
            4,
            Fluids.LAVA.getDefaultState().getBlockState()
        ));
    }
}
