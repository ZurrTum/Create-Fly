package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SawRenderState;
import com.zurrtum.create.compat.eiv.display.SawingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2f;

import java.util.List;

public class SawingView extends CreateView {
    private final List<SlotContent> results;
    private final List<Float> chances;
    private final SlotContent ingredient;

    public SawingView(SawingDisplay display) {
        results = display.results.stream().map(SlotContent::of).toList();
        chances = display.chances;
        ingredient = SlotContent.of(display.ingredient);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.SAWING;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(ingredient);
    }

    @Override
    public List<SlotContent> getResults() {
        return results;
    }

    @Override
    public int placeViewSlots(SlotDefinition slotDefinition) {
        int i = 0;
        for (int size = results.size(); i < size; i++) {
            slotDefinition.addItemSlot(i, i % 2 == 0 ? 118 : 137, 44 + (i / 2) * -19);
        }
        slotDefinition.addItemSlot(i++, 44, 1);
        return i;
    }

    @Override
    public int bindViewSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
        int i = 0;
        for (int size = results.size(); i < size; i++) {
            float chance = chances.get(i);
            if (chance == 1) {
                slotFillContext.bindOptionalSlot(i, results.get(i), SLOT);
            } else {
                bindChanceSlot(slotFillContext, i, results.get(i), chance);
            }
        }
        slotFillContext.bindOptionalSlot(i++, ingredient, SLOT);
        return i;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, DrawContext context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 70, 2);
        AllGuiTextures.JEI_SHADOW.render(context, 55, 51);
        context.state.addSpecialElement(new SawRenderState(new Matrix3x2f(context.getMatrices()), 64, 27));
    }
}
