package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.ManualBlockRenderState;
import com.zurrtum.create.compat.eiv.display.ManualApplicationDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.joml.Matrix3x2f;

import java.util.List;

public class ManualApplicationView extends CreateView {
    private final SlotContent result;
    private final SlotContent target;
    private final SlotContent ingredient;
    private final boolean keepHeldItem;

    public ManualApplicationView(ManualApplicationDisplay display) {
        result = SlotContent.of(display.result);
        target = SlotContent.of(display.target);
        ingredient = SlotContent.of(display.ingredient);
        keepHeldItem = display.keepHeldItem;
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.ITEM_APPLICATION;
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
        slotDefinition.addItemSlot(0, 51, 1);
        slotDefinition.addItemSlot(1, 27, 34);
        slotDefinition.addItemSlot(2, 132, 34);
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
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, DrawContext context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_SHADOW.render(context, 67, 48);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 79, 11);
        ItemStack stack = target.getByIndex(target.index());
        if (stack.getItem() instanceof BlockItem blockItem) {
            BlockState block = blockItem.getBlock().getDefaultState();
            context.state.addSpecialElement(new ManualBlockRenderState(new Matrix3x2f(context.getMatrices()), block, 79, 30));
        }
    }
}
