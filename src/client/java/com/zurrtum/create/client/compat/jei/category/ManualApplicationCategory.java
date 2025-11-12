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
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.List;

public class ManualApplicationCategory extends CreateCategory<RecipeHolder<ManualApplicationRecipe>> {
    public static List<RecipeHolder<ManualApplicationRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(AllRecipeTypes.ITEM_APPLICATION).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeHolder<ManualApplicationRecipe>> getRecipeType() {
        return JeiClientPlugin.ITEM_APPLICATION;
    }

    @Override
    @NotNull
    public Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ManualApplicationRecipe> entry, IFocusGroup focuses) {
        ManualApplicationRecipe recipe = entry.value();
        builder.addInputSlot(51, 5).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        builder.addInputSlot(27, 38).setSlotName("target").setBackground(SLOT, -1, -1).add(recipe.target());
        builder.addOutputSlot(132, 38).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(
        RecipeHolder<ManualApplicationRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        GuiGraphics graphics,
        double mouseX,
        double mouseY
    ) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 67, 52);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 79, 15);
        recipeSlotsView.findSlotByName("target").flatMap(IRecipeSlotView::getDisplayedItemStack).ifPresent(stack -> {
            if (stack.getItem() instanceof BlockItem blockItem) {
                BlockState block = blockItem.getBlock().defaultBlockState();
                graphics.guiRenderState.submitPicturesInPictureState(new ManualBlockRenderState(new Matrix3x2f(graphics.pose()), block, 79, 34));
            }
        });
    }
}
