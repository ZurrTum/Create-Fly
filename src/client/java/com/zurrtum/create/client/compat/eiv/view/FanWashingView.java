package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import com.zurrtum.create.compat.eiv.display.FanWashingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.material.Fluids;
import org.joml.Matrix3x2f;

import java.util.List;

public class FanWashingView extends CreateView {
    private final List<SlotContent> results;
    private final SlotContent ingredient;
    private final List<Float> chances;

    public FanWashingView(FanWashingDisplay display) {
        results = display.results.stream().map(SlotContent::of).toList();
        chances = display.chances;
        ingredient = SlotContent.of(display.ingredient);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.FAN_WASHING;
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
        int size = results.size();
        if (size == 1) {
            slotDefinition.addItemSlot(0, 17, 44);
            slotDefinition.addItemSlot(1, 137, 44);
            return 2;
        } else {
            int xOffsetAmount = 1 - Math.min(3, size);
            slotDefinition.addItemSlot(0, 17 + xOffsetAmount * 5, 44);
            for (int i = 0, left = 137 + xOffsetAmount * 9, top = 44; i < size; i++) {
                slotDefinition.addItemSlot(i + 1, left + (i % 3) * 19, top + (i / 3) * -19);
            }
            return size + 1;
        }
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
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        int xOffsetAmount = 1 - Math.min(3, results.size());
        AllGuiTextures.JEI_SHADOW.render(context, 42, 23);
        AllGuiTextures.JEI_LIGHT.render(context, 61, 35);
        AllGuiTextures.JEI_LONG_ARROW.render(context, 50 + 7 * xOffsetAmount, 47);
        context.guiRenderState.submitPicturesInPictureState(new FanRenderState(
            new Matrix3x2f(context.pose()),
            52,
            0,
            Fluids.WATER.defaultFluidState().createLegacyBlock()
        ));
    }
}
