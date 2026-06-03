package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.SandPaperPolishingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SandPaperRenderState;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

import java.util.List;

public class SandPaperPolishingView extends CreateView {
    private final Identifier id;
    private final SlotContent result;
    private final SlotContent ingredient;

    public SandPaperPolishingView(Identifier id, SandPaperPolishingRecipe recipe) {
        this.id = id;
        result = SlotContent.of(recipe.result());
        ingredient = SlotContent.of(recipe.ingredient());
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return SandPaperPolishingCategory.INSTANCE;
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
        slotDefinition.addItemSlot(0, 27, 31);
        slotDefinition.addItemSlot(1, 132, 31);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, result, SLOT);
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
        AllGuiTextures.JEI_SHADOW.render(context, 61, 23);
        AllGuiTextures.JEI_LONG_ARROW.render(context, 52, 34);
        ItemStack stack = ingredient.getByIndex(ingredient.index());
        context.guiRenderState.addPicturesInPictureState(new SandPaperRenderState(
            new Matrix3x2f(context.pose()),
            stack,
            74,
            0
        ));
    }
}
