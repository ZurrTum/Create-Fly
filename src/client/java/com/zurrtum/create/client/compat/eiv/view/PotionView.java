package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.display.PotionDisplay;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2f;

import java.util.List;

public class PotionView extends CreateView {
    private final SlotContent result;
    private final SlotContent ingredient;
    private final SlotContent fluidIngredient;

    public PotionView(PotionDisplay display) {
        result = SlotContent.of(getItemStack(display.result));
        ingredient = SlotContent.of(display.ingredient);
        fluidIngredient = SlotContent.of(getItemStacks(display.fluidIngredient));
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.AUTOMATIC_BREWING;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(ingredient, fluidIngredient);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(result);
    }

    @Override
    public void placeSlots(SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 21, 51);
        slotDefinition.addItemSlot(1, 40, 51);
        slotDefinition.addItemSlot(2, 142, 51);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, fluidIngredient, SLOT);
        slotFillContext.bindOptionalSlot(2, result, SLOT);
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        HeatCondition requiredHeat = HeatCondition.HEATED;
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 136, 32);
        Matrix3x2f pose = new Matrix3x2f(context.pose());
        AllGuiTextures.JEI_HEAT_BAR.render(context, 0, 80);
        AllGuiTextures.JEI_LIGHT.render(context, 77, 88);
        context.guiRenderState.submitPicturesInPictureState(new BasinBlazeBurnerRenderState(pose, 87, 69, requiredHeat.visualizeAsBlazeBurner()));
        context.guiRenderState.submitPicturesInPictureState(new MixingBasinRenderState(pose, 87, -5));
        context.drawString(
            context.minecraft.font,
            CreateLang.translateDirect(requiredHeat.getTranslationKey()),
            5,
            86,
            requiredHeat.getColor(),
            false
        );
    }
}
