package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.CrushWheelRenderState;
import com.zurrtum.create.compat.eiv.display.CrushingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2f;

import java.util.List;

public class CrushingView extends CreateView {
    private final List<SlotContent> results;
    private final SlotContent ingredient;
    private final List<Float> chances;

    public CrushingView(CrushingDisplay display) {
        results = display.results.stream().map(SlotContent::of).toList();
        chances = display.chances;
        ingredient = SlotContent.of(display.ingredient);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.CRUSHING;
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
    protected int placeViewSlots(RecipeViewMenu.SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 51, 1);
        int size = results.size();
        for (int i = 0, start = (179 - 19 * size) / 2 + 3; i < size; i++) {
            slotDefinition.addItemSlot(i + 1, start + i * 19, 81);
        }
        return size + 1;
    }

    @Override
    protected int bindViewSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
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
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 72, 5);
        context.state.addSpecialElement(new CrushWheelRenderState(new Matrix3x2f(context.getMatrices()), 42, 22));
    }
}
