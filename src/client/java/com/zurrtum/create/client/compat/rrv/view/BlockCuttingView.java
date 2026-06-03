package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.BlockCuttingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SawRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class BlockCuttingView extends CreateView {
    private final Identifier id;
    private final SlotContent ingredient;
    private final List<SlotContent> results;

    public BlockCuttingView(Identifier id, Ingredient input, List<List<ItemStack>> outputs) {
        this.id = id;
        ingredient = SlotContent.of(input);
        int size = outputs.size();
        results = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            results.add(SlotContent.of(outputs.get(i)));
        }
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return BlockCuttingCategory.INSTANCE;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(ingredient);
    }

    @Override
    public List<SlotContent> getResults() {
        return results;
    }

    @Override
    protected int placeViewSlots(SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 3, 1);
        int size = results.size();
        for (int i = 0, left = 76, top = 44; i < size; i++) {
            slotDefinition.addItemSlot(i + 1, left + (i % 5) * 19, top + (i / 5) * -19);
        }
        return size + 1;
    }

    @Override
    protected int bindViewSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        int size = results.size();
        for (int i = 0; i < size; i++) {
            slotFillContext.bindOptionalSlot(i + 1, results.get(i), SLOT);
        }
        return size + 1;
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
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 29, 2);
        AllGuiTextures.JEI_SHADOW.render(context, 14, 46);
        context.guiRenderState.addPicturesInPictureState(new SawRenderState(new Matrix3x2f(context.pose()), 23, 22));
    }
}
