package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.compat.eiv.display.CompactingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2f;

import java.util.List;

public class CompactingView extends CreateView {
    private final SlotContent result;
    private final List<SlotContent> ingredients;

    public CompactingView(CompactingDisplay display) {
        result = SlotContent.of(display.result);
        ingredients = display.ingredients.stream().map(SlotContent::of).toList();
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.PACKING;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return ingredients;
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(result);
    }

    @Override
    public int placeViewSlots(SlotDefinition slotDefinition) {
        int i = 0;
        for (int size = ingredients.size(), xOffset = size < 3 ? 12 + (3 - size) * 19 / 2 : 12; i < size; i++) {
            slotDefinition.addItemSlot(i, xOffset + (i % 3) * 19, 49 - (i / 3) * 19);
        }
        slotDefinition.addItemSlot(i++, 142, 49);
        return i;
    }

    @Override
    public int bindViewSlots(SlotFillContext slotFillContext) {
        int i = 0;
        for (int size = ingredients.size(); i < size; i++) {
            slotFillContext.bindOptionalSlot(i, ingredients.get(i), SLOT);
        }
        slotFillContext.bindOptionalSlot(i++, result, SLOT);
        return i;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, DrawContext context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 136, 30);
        AllGuiTextures.JEI_SHADOW.render(context, 81, 66);
        context.state.addSpecialElement(new PressBasinRenderState(new Matrix3x2f(context.getMatrices()), 91, -7));
    }
}
