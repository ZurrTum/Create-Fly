package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.IconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.ManualBlockRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BlockItem;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.List;

public class ManualApplicationCategory extends CreateCategory<RecipeEntry<ManualApplicationRecipe>> {
    public static List<RecipeEntry<ManualApplicationRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        return preparedRecipes.getAll(AllRecipeTypes.ITEM_APPLICATION).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<ManualApplicationRecipe>> getRecipeType() {
        return JeiClientPlugin.ITEM_APPLICATION;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.item_application");
    }

    @Override
    public IDrawable getIcon() {
        return new IconRenderer(AllItems.BRASS_HAND);
    }

    @Override
    public int getHeight() {
        return 60;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<ManualApplicationRecipe> entry, IFocusGroup focuses) {
        ManualApplicationRecipe recipe = entry.value();
        IRecipeSlotBuilder slot = builder.addInputSlot(51, 5).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        if (recipe.keepHeldItem()) {
            slot.addRichTooltipCallback(KEEP_HELD);
        }
        builder.addInputSlot(27, 38).setSlotName("target").setBackground(SLOT, -1, -1).add(recipe.target());
        List<ProcessingOutput> results = recipe.results();
        int size = results.size();
        if (size == 1) {
            addChanceSlot(builder, 132, 38, results.getFirst());
        } else {
            for (int i = 0; i < size; i++) {
                addChanceSlot(builder, i % 2 == 0 ? 132 : 151, 38 + (i / 2) * -19, results.get(i));
            }
        }
    }

    @Override
    public void draw(
        RecipeEntry<ManualApplicationRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        DrawContext graphics,
        double mouseX,
        double mouseY
    ) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 67, 52);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 79, 15);
        recipeSlotsView.findSlotByName("target").flatMap(IRecipeSlotView::getDisplayedItemStack).ifPresent(stack -> {
            if (stack.getItem() instanceof BlockItem blockItem) {
                BlockState block = blockItem.getBlock().getDefaultState();
                graphics.state.addSpecialElement(new ManualBlockRenderState(new Matrix3x2f(graphics.getMatrices()), block, 79, 34));
            }
        });
    }
}
