package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import com.zurrtum.create.compat.eiv.display.FanBlastingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.material.Fluids;
import org.joml.Matrix3x2f;

import java.util.List;

public class FanBlastingView extends CreateView {
    private final SlotContent result;
    private final SlotContent ingredient;

    public FanBlastingView(FanBlastingDisplay display) {
        result = SlotContent.of(display.result);
        ingredient = SlotContent.of(display.ingredient);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.FAN_BLASTING;
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
        slotDefinition.addItemSlot(0, 17, 44);
        slotDefinition.addItemSlot(1, 137, 44);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, result, SLOT);
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_SHADOW.render(context, 42, 23);
        AllGuiTextures.JEI_LIGHT.render(context, 61, 35);
        AllGuiTextures.JEI_LONG_ARROW.render(context, 50, 47);
        context.guiRenderState.submitPicturesInPictureState(new FanRenderState(
            new Matrix3x2f(context.pose()),
            52,
            0,
            Fluids.LAVA.defaultFluidState().createLegacyBlock()
        ));
    }
}
