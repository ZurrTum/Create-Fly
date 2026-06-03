package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.FanSmokingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix3x2f;

import java.util.List;

public class FanSmokingView extends CreateView {
    private final Identifier id;
    private final SlotContent result;
    private final SlotContent ingredient;

    public FanSmokingView(Identifier id, SmokingRecipe recipe) {
        this.id = id;
        result = SlotContent.of(recipe.result());
        ingredient = SlotContent.of(recipe.input());
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return FanSmokingCategory.INSTANCE;
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
        slotDefinition.addItemSlot(0, 17, 55);
        slotDefinition.addItemSlot(1, 137, 55);
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
        AllGuiTextures.JEI_SHADOW.render(context, 42, 34);
        AllGuiTextures.JEI_LIGHT.render(context, 61, 46);
        AllGuiTextures.JEI_LONG_ARROW.render(context, 50, 58);
        context.guiRenderState.addPicturesInPictureState(new FanRenderState(
            new Matrix3x2f(context.pose()),
            52,
            11,
            Blocks.FIRE.defaultBlockState()
        ));
    }
}
