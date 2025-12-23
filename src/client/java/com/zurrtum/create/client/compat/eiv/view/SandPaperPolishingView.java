package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SandPaperRenderState;
import com.zurrtum.create.compat.eiv.display.SandPaperPolishingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

import java.util.List;

public class SandPaperPolishingView extends CreateView {
    private final SlotContent result;
    private final SlotContent ingredient;

    public SandPaperPolishingView(SandPaperPolishingDisplay display) {
        result = SlotContent.of(display.result);
        ingredient = SlotContent.of(display.ingredient);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.SANDPAPER_POLISHING;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(ingredient);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(result);
    }

    @Override
    public void placeSlots(SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 27, 31);
        slotDefinition.addItemSlot(1, 132, 31);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, result, SLOT);
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_SHADOW.render(context, 61, 23);
        AllGuiTextures.JEI_LONG_ARROW.render(context, 52, 34);
        ItemStack stack = ingredient.getByIndex(ingredient.index());
        context.guiRenderState.submitPicturesInPictureState(new SandPaperRenderState(new Matrix3x2f(context.pose()), stack, 74, 0));
    }
}
