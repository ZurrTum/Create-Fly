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
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2f;

import java.util.List;

public class DeployingView extends CreateView {
    private final SlotContent result;
    private final SlotContent target;
    private final SlotContent ingredient;
    private final boolean keepHeldItem;

    public DeployingView(DeployingDisplay display) {
        result = SlotContent.of(display.result);
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
        return List.of(result);
    }

    @Override
    public void placeSlots(SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 51, 7);
        slotDefinition.addItemSlot(1, 27, 53);
        slotDefinition.addItemSlot(2, 132, 53);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        if (keepHeldItem) {
            slotFillContext.addAdditionalStackModifier(0, NOT_CONSUMED);
        }
        slotFillContext.bindOptionalSlot(1, target, SLOT);
        slotFillContext.bindOptionalSlot(2, result, SLOT);
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_SHADOW.render(context, 62, 59);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 126, 31);
        context.guiRenderState.submitPicturesInPictureState(new DeployerRenderState(new Matrix3x2f(context.pose()), 75, -8));
    }
}
