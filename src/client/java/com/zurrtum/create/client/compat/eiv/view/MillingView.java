package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.MillstoneRenderState;
import com.zurrtum.create.compat.eiv.display.CrushingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2f;

import java.util.List;

public class MillingView extends CreateView {
    private final List<SlotContent> results;
    private final SlotContent ingredient;
    private final List<Float> chances;

    public MillingView(CrushingDisplay display) {
        results = display.results.stream().map(SlotContent::of).toList();
        chances = display.chances;
        ingredient = SlotContent.of(display.ingredient);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.MILLING;
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
    protected int placeViewSlots(SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 8, 5);
        int size = results.size();
        if (size == 1) {
            slotDefinition.addItemSlot(1, 132, 23);
        } else {
            for (int i = 0; i < size; i++) {
                slotDefinition.addItemSlot(i + 1, i % 2 == 0 ? 126 : 145, 23 + (i / 2) * -19);
            }
        }
        return size + 1;
    }

    @Override
    protected int bindViewSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        int size = results.size();
        for (int i = 0; i < size; i++) {
            float chance = chances.get(i);
            if (chance == 1) {
                slotFillContext.bindOptionalSlot(i + 1, results.get(i), SLOT);
            } else {
                bindChanceSlot(slotFillContext, i + 1, results.get(i), chance);
            }
        }
        return size + 1;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, DrawContext context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_ARROW.render(context, 78, 28);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 36, 0);
        AllGuiTextures.JEI_SHADOW.render(context, 25, 36);
        context.state.addSpecialElement(new MillstoneRenderState(new Matrix3x2f(context.getMatrices()), 35, 15));
    }
}
