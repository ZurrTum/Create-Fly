package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.CrafterRenderState;
import com.zurrtum.create.compat.eiv.display.MechanicalCraftingDisplay;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2f;

import java.util.List;

public class MechanicalCraftingView extends CreateView {
    private final SlotContent result;
    private final List<SlotContent> ingredients;
    private final int width;
    private final int height;
    private final IntSet empty;

    public MechanicalCraftingView(MechanicalCraftingDisplay display) {
        result = SlotContent.of(display.result);
        width = display.width;
        height = display.height;
        empty = new IntOpenHashSet(display.empty);
        ingredients = display.ingredients.stream().map(SlotContent::of).toList();
        for (int i = ingredients.size() + empty.size(), total = width * height; i < total; i++) {
            empty.add(i);
        }
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.MECHANICAL_CRAFTING;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return ingredients;
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(result);
    }

    @Override
    protected int placeViewSlots(SlotDefinition slotDefinition) {
        int left = 7;
        if (width < 5) {
            left += (19 * (5 - width)) / 2;
        }
        int top = 1;
        if (height < 5) {
            top += (19 * (5 - height)) / 2;
        }
        int i = 0;
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                if (empty.contains(h * width + w)) {
                    continue;
                }
                slotDefinition.addItemSlot(i++, left + 16 * w + (w * 3), top + 16 * h + (h * 3));
            }
        }
        slotDefinition.addItemSlot(i++, 133, 74);
        return i;
    }

    @Override
    protected int bindViewSlots(SlotFillContext slotFillContext) {
        int i = 0;
        for (int size = ingredients.size(); i < size; i++) {
            slotFillContext.bindOptionalSlot(i, ingredients.get(i), SLOT);
        }
        slotFillContext.bindOptionalSlot(i++, result, SLOT);
        return i;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, DrawContext context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 128, 53);
        AllGuiTextures.JEI_SHADOW.render(context, 113, 32);
        context.state.addSpecialElement(new CrafterRenderState(new Matrix3x2f(context.getMatrices()), 124, 12));
        String size = String.valueOf(ingredients.size());
        context.drawText(MinecraftClient.getInstance().textRenderer, size, 142, 33, 0xFFFFFFFF, true);
    }
}
