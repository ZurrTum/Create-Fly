package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.AutoMixingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class AutoMixingView extends CreateView {
    private final Identifier id;
    private final SlotContent result;
    private final List<SlotContent> ingredients;

    public AutoMixingView(Identifier id, ShapelessRecipe recipe) {
        this.id = id;
        result = SlotContent.of(recipe.result);
        List<Ingredient> list = recipe.ingredients;
        int size = list.size();
        ingredients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ingredients.add(SlotContent.of(list.get(i)));
        }
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return AutoMixingCategory.INSTANCE;
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
        int i = 0;
        for (int size = ingredients.size(), xOffset = size < 3 ? (3 - size) * 19 / 2 : 0; i < size; i++) {
            slotDefinition.addItemSlot(i, 12 + xOffset + (i % 3) * 19, 48 - (i / 3) * 19);
        }
        slotDefinition.addItemSlot(i++, 142, 48);
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
    public void renderRecipe(
        RecipeViewScreen screen,
        RecipePosition position,
        GuiGraphicsExtractor context,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 136, 29);
        AllGuiTextures.JEI_SHADOW.render(context, 81, 65);
        context.guiRenderState.addPicturesInPictureState(new MixingBasinRenderState(
            new Matrix3x2f(context.pose()),
            91,
            -8
        ));
    }
}
