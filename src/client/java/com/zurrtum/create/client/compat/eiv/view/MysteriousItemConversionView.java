package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.compat.eiv.display.MysteriousItemConversionDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class MysteriousItemConversionView extends CreateView {
    private final SlotContent ingredient;
    private final SlotContent result;

    public MysteriousItemConversionView(MysteriousItemConversionDisplay display) {
        ingredient = SlotContent.of(display.ingredient);
        result = SlotContent.of(display.result);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.MYSTERY_CONVERSION;
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
        slotDefinition.addItemSlot(0, 27, 12);
        slotDefinition.addItemSlot(1, 132, 12);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, result, SLOT);
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, DrawContext context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_LONG_ARROW.render(context, 52, 15);
        AllGuiTextures.JEI_QUESTION_MARK.render(context, 77, 0);
    }
}
