package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import com.zurrtum.create.compat.eiv.display.AutoMixingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2f;

import java.util.List;

public class AutoMixingView extends CreateView {
    private final SlotContent result;
    private final List<SlotContent> ingredients;

    public AutoMixingView(AutoMixingDisplay display) {
        result = SlotContent.of(display.result);
        ingredients = display.ingredients.stream().map(SlotContent::of).toList();
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.AUTOMATIC_SHAPELESS;
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
        for (int size = ingredients.size(), xOffset = size < 3 ? (3 - size) * 19 / 2 : 0; i < size; i++) {
            slotDefinition.addItemSlot(i, 12 + xOffset + (i % 3) * 19, 48 - (i / 3) * 19);
        }
        slotDefinition.addItemSlot(i++, 142, 48);
        return i;
    }

    @Override
    protected int bindViewSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
        int i = 0;
        for (int size = ingredients.size(); i < size; i++) {
            slotFillContext.bindOptionalSlot(i, ingredients.get(i), SLOT);
        }
        slotFillContext.bindOptionalSlot(i++, result, SLOT);
        return i;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 136, 29);
        AllGuiTextures.JEI_SHADOW.render(context, 81, 65);
        context.guiRenderState.submitPicturesInPictureState(new MixingBasinRenderState(new Matrix3x2f(context.pose()), 91, -8));
    }
}
