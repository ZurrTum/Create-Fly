package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.display.CompactingDisplay;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class CompactingView extends CreateView {
    private final List<SlotContent> results;
    private final List<Float> chances;
    private final List<SlotContent> ingredients;
    private final HeatCondition heat;
    private final SlotContent burner;
    private final SlotContent cake;

    public CompactingView(CompactingDisplay display) {
        results = display.results.stream().map(SlotContent::of).toList();
        chances = display.chances;
        ingredients = new ArrayList<>(display.ingredients.size() + display.fluidIngredients.size());
        for (List<ItemStack> ingredient : display.ingredients) {
            ingredients.add(SlotContent.of(ingredient));
        }
        for (FluidIngredient fluidIngredient : display.fluidIngredients) {
            ingredients.add(SlotContent.of(getItemStacks(fluidIngredient)));
        }
        heat = display.heat;
        burner = heat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE) ? null : SlotContent.of(AllItems.BLAZE_BURNER);
        cake = heat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED) ? null : SlotContent.of(AllItems.BLAZE_CAKE);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.PACKING;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return ingredients;
    }

    @Override
    public List<SlotContent> getResults() {
        return results;
    }

    @Override
    public int placeViewSlots(SlotDefinition slotDefinition) {
        int i = 0;
        for (int size = ingredients.size(), xOffset = size < 3 ? 8 + (3 - size) * 19 / 2 : 8, yOffset = size <= 9 ? 51 : 60; i < size; i++) {
            slotDefinition.addItemSlot(i, xOffset + (i % 3) * 19, yOffset - (i / 3) * 19);
        }
        int size = results.size();
        int end = size - 1;
        for (int j = 0; j < end; j++) {
            slotDefinition.addItemSlot(i++, j % 2 == 0 ? 128 : 147, 51 - 19 * (j / 2));
        }
        if (size % 2 != 0) {
            slotDefinition.addItemSlot(i++, 138, 51 - 19 * (end / 2));
        } else {
            slotDefinition.addItemSlot(i++, end % 2 == 0 ? 128 : 147, 51 - 19 * (end / 2));
        }
        if (burner != null) {
            slotDefinition.addItemSlot(i++, 130, 81);
        }
        if (cake != null) {
            slotDefinition.addItemSlot(i++, 149, 81);
        }
        return i;
    }

    @Override
    public int bindViewSlots(SlotFillContext slotFillContext) {
        int i = 0;
        for (int size = ingredients.size(); i < size; i++) {
            slotFillContext.bindOptionalSlot(i, ingredients.get(i), SLOT);
        }
        for (int j = 0, size = chances.size(); j < size; j++) {
            float chance = chances.get(j);
            if (chance == 1) {
                slotFillContext.bindOptionalSlot(i++, results.get(j), SLOT);
            } else {
                bindChanceSlot(slotFillContext, i++, results.get(j), chance);
            }
        }
        if (burner != null) {
            slotFillContext.bindSlot(i++, burner);
        }
        if (cake != null) {
            slotFillContext.bindSlot(i++, cake);
        }
        return i;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, DrawContext context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 132, 32 - (results.size() - 1) / 2 * 19);
        Matrix3x2f pose = new Matrix3x2f(context.getMatrices());
        if (heat == HeatCondition.NONE) {
            AllGuiTextures.JEI_NO_HEAT_BAR.render(context, 0, 80);
            AllGuiTextures.JEI_SHADOW.render(context, 77, 68);
        } else {
            AllGuiTextures.JEI_HEAT_BAR.render(context, 0, 80);
            AllGuiTextures.JEI_LIGHT.render(context, 77, 88);
            context.state.addSpecialElement(new BasinBlazeBurnerRenderState(pose, 87, 69, heat.visualizeAsBlazeBurner()));
        }
        context.state.addSpecialElement(new PressBasinRenderState(new Matrix3x2f(context.getMatrices()), 87, -5));
        context.drawText(
            context.client.textRenderer,
            CreateLang.translateDirect(heat.getTranslationKey()),
            5,
            86,
            heat.getColor(),
            false
        );
    }
}
