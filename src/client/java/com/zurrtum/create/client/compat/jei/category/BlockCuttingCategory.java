package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.display.BlockCuttingDisplay;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SawRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockCuttingCategory extends CreateCategory<BlockCuttingDisplay> {
    public static List<BlockCuttingDisplay> getRecipes(PreparedRecipes preparedRecipes) {
        Object2ObjectMap<Ingredient, Pair<Identifier, List<ItemStack>>> map = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
            public boolean equals(Ingredient ingredient, Ingredient other) {
                return Objects.equals(ingredient, other);
            }

            public int hashCode(Ingredient ingredient) {
                if (ingredient.entries instanceof RegistryEntryList.Direct<Item> direct) {
                    return direct.hashCode();
                }
                if (ingredient.entries instanceof RegistryEntryList.Named<Item> named) {
                    return named.getTag().id().hashCode();
                }
                return ingredient.hashCode();
            }
        });
        for (RecipeEntry<StonecuttingRecipe> entry : preparedRecipes.getAll(RecipeType.STONECUTTING)) {
            if (AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
                continue;
            }
            StonecuttingRecipe recipe = entry.value();
            map.computeIfAbsent(recipe.ingredient(), i -> Pair.of(entry.id().getValue(), new ArrayList<>())).getSecond().add(recipe.result());
        }
        List<BlockCuttingDisplay> recipes = new ArrayList<>();
        for (Object2ObjectMap.Entry<Ingredient, Pair<Identifier, List<ItemStack>>> entry : map.object2ObjectEntrySet()) {
            Pair<Identifier, List<ItemStack>> pair = entry.getValue();
            List<ItemStack> outputs = pair.getSecond();
            int size = outputs.size();
            if (size <= 15) {
                recipes.add(new BlockCuttingDisplay(pair.getFirst(), entry.getKey(), outputs.stream().map(List::of).toList()));
                continue;
            }
            List<List<ItemStack>> list = new ArrayList<>(15);
            for (int i = 0; i < 15; i++) {
                List<ItemStack> stacks = new ArrayList<>(2);
                stacks.add(outputs.get(i));
                list.add(stacks);
            }
            for (int i = 15; i < size; i++) {
                list.get(i % 15).add(outputs.get(i));
            }
            recipes.add(new BlockCuttingDisplay(pair.getFirst(), entry.getKey(), list));
        }
        return recipes;
    }

    @Override
    public Identifier getRegistryName(BlockCuttingDisplay display) {
        return display.id();
    }

    @Override
    @NotNull
    public IRecipeType<BlockCuttingDisplay> getRecipeType() {
        return JeiClientPlugin.BLOCK_CUTTING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.block_cutting");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_SAW, Items.STONE_BRICK_STAIRS);
    }

    @Override
    public int getHeight() {
        return 70;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockCuttingDisplay display, IFocusGroup focuses) {
        builder.addInputSlot(5, 5).setBackground(SLOT, -1, -1).add(display.input());
        List<List<ItemStack>> outputs = display.outputs();
        for (int i = 0, left = 78, top = 48, size = outputs.size(); i < size; i++) {
            builder.addOutputSlot(left + (i % 5) * 19, top + (i / 5) * -19).setBackground(SLOT, -1, -1).addItemStacks(outputs.get(i));
        }
    }

    @Override
    public void draw(BlockCuttingDisplay recipe, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 31, 6);
        AllGuiTextures.JEI_SHADOW.render(graphics, 16, 50);
        graphics.state.addSpecialElement(new SawRenderState(new Matrix3x2f(graphics.getMatrices()), 25, 26));
    }
}
