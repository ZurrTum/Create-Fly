package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SawRenderState;
import com.zurrtum.create.compat.eiv.display.BlockCuttingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2f;

import java.util.List;

public class BlockCuttingView extends CreateView {
    private final SlotContent ingredient;
    private final List<SlotContent> results;

    public BlockCuttingView(BlockCuttingDisplay display) {
        ingredient = SlotContent.of(display.ingredient);
        results = display.results.stream().map(SlotContent::of).toList();
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.BLOCK_CUTTING;
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
        slotDefinition.addItemSlot(0, 3, 1);
        int size = results.size();
        for (int i = 0, left = 76, top = 44; i < size; i++) {
            slotDefinition.addItemSlot(i + 1, left + (i % 5) * 19, top + (i / 5) * -19);
        }
        return size + 1;
    }

    @Override
    protected int bindViewSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        int size = results.size();
        for (int i = 0; i < size; i++) {
            slotFillContext.bindOptionalSlot(i + 1, results.get(i), SLOT);
        }
        return size + 1;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 29, 2);
        AllGuiTextures.JEI_SHADOW.render(context, 14, 46);
        context.guiRenderState.submitPicturesInPictureState(new SawRenderState(new Matrix3x2f(context.pose()), 23, 22));
    }
}
