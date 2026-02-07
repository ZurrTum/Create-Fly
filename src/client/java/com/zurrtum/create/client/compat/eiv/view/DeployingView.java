package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.DeployerRenderState;
import com.zurrtum.create.compat.eiv.display.DeployingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2f;

import java.util.List;

public class DeployingView extends CreateView {
    private final List<SlotContent> results;
    private final List<Float> chances;
    private final SlotContent target;
    private final SlotContent ingredient;
    private final boolean keepHeldItem;

    public DeployingView(DeployingDisplay display) {
        results = display.results.stream().map(SlotContent::of).toList();
        chances = display.chances;
        target = SlotContent.of(display.target);
        ingredient = SlotContent.of(display.ingredient);
        keepHeldItem = display.keepHeldItem;
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.DEPLOYING;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(ingredient, target);
    }

    @Override
    public List<SlotContent> getResults() {
        return results;
    }

    @Override
    public int placeViewSlots(SlotDefinition slotDefinition) {
        int i = 0;
        int size = results.size();
        if (size == 1) {
            slotDefinition.addItemSlot(i++, 132, 53);
        } else {
            for (; i < size; i++) {
                slotDefinition.addItemSlot(i, i % 2 == 0 ? 122 : 141, 53 + (i / 2) * -19);
            }
        }
        slotDefinition.addItemSlot(i++, 51, 7);
        slotDefinition.addItemSlot(i++, 27, 53);
        return i;
    }

    @Override
    public int bindViewSlots(SlotFillContext slotFillContext) {
        int i = 0;
        for (int size = results.size(); i < size; i++) {
            float chance = chances.get(i);
            if (chance == 1) {
                slotFillContext.bindOptionalSlot(i, results.get(i), SLOT);
            } else {
                bindChanceSlot(slotFillContext, i, results.get(i), chance);
            }
        }
        if (keepHeldItem) {
            slotFillContext.addAdditionalStackModifier(i, NOT_CONSUMED);
        }
        slotFillContext.bindOptionalSlot(i++, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(i++, target, SLOT);
        return i;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, DrawContext context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_SHADOW.render(context, 62, 59);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 126, results.size() <= 2 ? 31 : 12);
        context.state.addSpecialElement(new DeployerRenderState(new Matrix3x2f(context.getMatrices()), 75, -8));
    }
}
