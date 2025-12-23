package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.compat.eiv.display.AutoCompactingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class AutoCompactingView extends CreateView {
    private final SlotContent result;
    private final List<SlotContent> ingredients;

    public AutoCompactingView(AutoCompactingDisplay display) {
        result = SlotContent.of(display.result);
        int size = display.size;
        ingredients = new ArrayList<>(size);
        SlotContent ingredient = SlotContent.of(display.ingredient);
        for (int i = 0; i < size; i++) {
            ingredients.add(ingredient);
        }
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.AUTOMATIC_PACKING;
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
    protected int placeViewSlots(SlotDefinition slotDefinition) {
        int i = 0;
        for (int size = ingredients.size(), rows = size == 4 ? 2 : 3; i < size; i++) {
            slotDefinition.addItemSlot(i, (rows == 2 ? 27 : 18) + (i % rows) * 19, 49 - (i / rows) * 19);
        }
        slotDefinition.addItemSlot(i++, 142, 49);
        return i;
    }

    @Override
    protected int bindViewSlots(SlotFillContext slotFillContext) {
        int i = 0;
        for (int size = ingredients.size(); i < size; i++) {
            slotFillContext.bindOptionalSlot(i, ingredients.get(i), SLOT);
        }
        slotFillContext.bindOptionalSlot(i++, result, SLOT);
        return i;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 136, 30);
        AllGuiTextures.JEI_SHADOW.render(context, 81, 66);
        context.guiRenderState.submitPicturesInPictureState(new PressBasinRenderState(new Matrix3x2f(context.pose()), 91, -7));
    }
}
