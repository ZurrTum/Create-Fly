package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SpoutRenderState;
import com.zurrtum.create.compat.eiv.display.SpoutFillingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import de.crafty.eiv.common.recipe.item.FluidItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.joml.Matrix3x2f;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SpoutFillingView extends CreateView {
    public static final int MAX = 3;
    public static AtomicInteger idGenerator = new AtomicInteger();
    private final SlotContent result;
    private final SlotContent fluidIngredient;
    private final SlotContent ingredient;

    public SpoutFillingView(SpoutFillingDisplay display) {
        result = SlotContent.of(display.result);
        fluidIngredient = SlotContent.of(getItemStacks(display.fluidIngredient));
        ingredient = SlotContent.of(display.ingredient);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.SPOUT_FILLING;
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
        slotDefinition.addItemSlot(0, 27, 49);
        slotDefinition.addItemSlot(1, 27, 30);
        slotDefinition.addItemSlot(2, 132, 49);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, fluidIngredient, SLOT);
        slotFillContext.bindOptionalSlot(2, result, SLOT);
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, DrawContext context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_SHADOW.render(context, 62, 55);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 126, 27);
        ItemStack stack = fluidIngredient.getByIndex(fluidIngredient.index());
        if (stack.getItem() instanceof FluidItem item) {
            int i = idGenerator.getAndIncrement();
            if (i >= MAX) {
                idGenerator.set(0);
            }
            context.state.addSpecialElement(new SpoutRenderState(
                i,
                new Matrix3x2f(context.getMatrices()),
                item.getFluid(),
                stack.getComponentChanges(),
                75,
                -1,
                0
            ));
        }
    }
}
