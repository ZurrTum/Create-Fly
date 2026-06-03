package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.CrushingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.CrushWheelRenderState;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class CrushingView extends CreateView {
    private final Identifier id;
    private final List<SlotContent> results;
    private final SlotContent ingredient;
    private final FloatList chances;

    public CrushingView(Identifier id, CreateSingleStackRollableRecipe recipe) {
        this.id = id;
        List<ProcessingOutput> results = recipe.results();
        int size = results.size();
        this.results = new ArrayList<>(size);
        chances = new FloatArrayList(size);
        for (int i = 0; i < size; i++) {
            ProcessingOutput output = results.get(i);
            this.results.add(SlotContent.of(output.create()));
            chances.add(output.chance());
        }
        ingredient = SlotContent.of(recipe.ingredient());
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return CrushingCategory.INSTANCE;
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
        slotDefinition.addItemSlot(0, 51, 1);
        int size = results.size();
        for (int i = 0, start = (179 - 19 * size) / 2 + 3; i < size; i++) {
            slotDefinition.addItemSlot(i + 1, start + i * 19, 81);
        }
        return size + 1;
    }

    @Override
    protected int bindViewSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        int size = results.size();
        for (int i = 0; i < size; i++) {
            float chance = chances.getFloat(i);
            if (chance == 1) {
                slotFillContext.bindOptionalSlot(i + 1, results.get(i), SLOT);
            } else {
                bindChanceSlot(slotFillContext, i + 1, results.get(i), chance);
            }
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
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 72, 5);
        context.guiRenderState.addPicturesInPictureState(new CrushWheelRenderState(
            new Matrix3x2f(context.pose()),
            42,
            22
        ));
    }
}
