package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.PotionCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

import java.util.List;

public class PotionView extends CreateView {
    private final Identifier id;
    private final SlotContent result;
    private final SlotContent ingredient;
    private final SlotContent fluidIngredient;
    private final SlotContent burner;

    public PotionView(Identifier id, PotionRecipe recipe) {
        this.id = id;
        result = createSlot(recipe.result());
        ingredient = SlotContent.of(recipe.ingredient());
        fluidIngredient = createSlot(recipe.fluidIngredient());
        burner = SlotContent.of(AllItems.BLAZE_BURNER);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return PotionCategory.INSTANCE;
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
        slotDefinition.addItemSlot(3, 130, 81);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, fluidIngredient, SLOT);
        slotFillContext.bindOptionalSlot(2, result, SLOT);
        slotFillContext.bindSlot(3, burner);
    }

    @Override
    public void renderRecipe(
        RecipeViewScreen screen,
        RecipePosition position,
        GuiGraphicsExtractor context,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        HeatCondition requiredHeat = HeatCondition.HEATED;
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 136, 32);
        Matrix3x2f pose = new Matrix3x2f(context.pose());
        AllGuiTextures.JEI_HEAT_BAR.render(context, 0, 80);
        AllGuiTextures.JEI_LIGHT.render(context, 77, 88);
        context.guiRenderState.addPicturesInPictureState(new BasinBlazeBurnerRenderState(
            pose,
            87,
            69,
            requiredHeat.visualizeAsBlazeBurner()
        ));
        context.guiRenderState.addPicturesInPictureState(new MixingBasinRenderState(pose, 87, -5));
        context.text(
            screen.getFont(),
            CreateLang.translateDirect(requiredHeat.getTranslationKey()),
            5,
            86,
            requiredHeat.getColor(),
            false
        );
    }
}
