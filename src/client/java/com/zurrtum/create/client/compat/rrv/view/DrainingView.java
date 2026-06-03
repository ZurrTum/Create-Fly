package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import cc.cassian.rrv.common.recipe.item.FluidItem;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.DrainingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.DrainRenderState;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

import java.util.List;

public class DrainingView extends CreateView {
    private final Identifier id;
    private final SlotContent result;
    private final SlotContent fluidResult;
    private final SlotContent ingredient;

    public DrainingView(Identifier id, EmptyingRecipe recipe) {
        this.id = id;
        result = SlotContent.of(recipe.result());
        fluidResult = createSlot(recipe.fluidResult());
        ingredient = SlotContent.of(recipe.ingredient());
    }

    public DrainingView(Identifier id, ItemStack result, FluidStack fluidResult, ItemStack ingredient) {
        this.id = id;
        this.result = SlotContent.of(result);
        this.fluidResult = createSlot(fluidResult);
        this.ingredient = SlotContent.of(ingredient);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return DrainingCategory.INSTANCE;
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
    public void renderRecipe(
        RecipeViewScreen screen,
        RecipePosition position,
        GuiGraphicsExtractor context,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        AllGuiTextures.JEI_SHADOW.render(context, 62, 33);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 73, 0);
        ItemStack stack = fluidResult.getByIndex(fluidResult.index());
        if (stack.getItem() instanceof FluidItem item) {
            context.guiRenderState.addPicturesInPictureState(new DrainRenderState(
                new Matrix3x2f(context.pose()),
                item.getFluid(),
                stack.getComponentsPatch(),
                75,
                19
            ));
        }
    }
}
