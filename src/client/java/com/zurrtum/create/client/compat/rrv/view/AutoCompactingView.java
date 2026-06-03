package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.AutoCompactingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AutoCompactingView extends CreateView {
    private final Identifier id;
    private final SlotContent result;
    private final List<SlotContent> ingredients;

    public AutoCompactingView(Identifier id, ShapelessRecipe recipe) {
        this.id = id;
        result = SlotContent.of(recipe.result);
        List<Ingredient> list = recipe.ingredients;
        int size = list.size();
        ingredients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ingredients.add(SlotContent.of(list.get(i)));
        }
    }

    public AutoCompactingView(Identifier id, ShapedRecipe recipe) {
        this.id = id;
        result = SlotContent.of(recipe.result);
        List<Optional<Ingredient>> list = recipe.getIngredients();
        int size = list.size();
        ingredients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.get(i).ifPresent(ingredient -> ingredients.add(SlotContent.of(ingredient)));
        }
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return AutoCompactingCategory.INSTANCE;
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
        for (int size = ingredients.size(), rows = size == 4 ? 2 : 3; i < size; i++) {
            slotDefinition.addItemSlot(i, (rows == 2 ? 27 : 18) + (i % rows) * 19, 49 - (i / rows) * 19);
        }
        slotDefinition.addItemSlot(i++, 142, 49);
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
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 136, 30);
        AllGuiTextures.JEI_SHADOW.render(context, 81, 66);
        context.guiRenderState.addPicturesInPictureState(new PressBasinRenderState(
            new Matrix3x2f(context.pose()),
            91,
            -7
        ));
    }
}
