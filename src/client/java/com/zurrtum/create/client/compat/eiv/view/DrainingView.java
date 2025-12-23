package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.DrainRenderState;
import com.zurrtum.create.compat.eiv.display.DrainingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import de.crafty.eiv.common.recipe.item.FluidItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

import java.util.List;

public class DrainingView extends CreateView {
    private final SlotContent result;
    private final SlotContent fluidResult;
    private final SlotContent ingredient;

    public DrainingView(DrainingDisplay display) {
        result = SlotContent.of(display.result);
        fluidResult = SlotContent.of(getItemStack(display.fluidResult));
        ingredient = SlotContent.of(display.ingredient);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.DRAINING;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(ingredient);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(result, fluidResult);
    }

    @Override
    public void placeSlots(SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 27, 4);
        slotDefinition.addItemSlot(1, 132, 4);
        slotDefinition.addItemSlot(2, 132, 23);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, fluidResult, SLOT);
        slotFillContext.bindOptionalSlot(2, result, SLOT);
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_SHADOW.render(context, 62, 33);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 73, 0);
        ItemStack stack = fluidResult.getByIndex(fluidResult.index());
        if (stack.getItem() instanceof FluidItem item) {
            context.guiRenderState.submitPicturesInPictureState(new DrainRenderState(
                new Matrix3x2f(context.pose()),
                item.getFluid(),
                stack.getComponentsPatch(),
                75,
                19
            ));
        }
    }
}
