package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.MillingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.MillstoneRenderState;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MillingView extends CreateView {
    private final Identifier id;
    private final List<SlotContent> results;
    private final SlotContent ingredient;
    private final FloatList chances;

    public MillingView(Identifier id, MillingRecipe recipe) {
        this.id = id;
        List<ProcessingOutput> results = recipe.results();
        int size = results.size();
        this.results = new ArrayList<>(size);
        chances = new FloatArrayList(size);
        for (int i = 0; i < size; i++) {
            ProcessingOutput result = results.get(i);
            this.results.add(SlotContent.of(result.create()));
            chances.add(result.chance());
        }
        ingredient = SlotContent.of(recipe.ingredient());
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return MillingCategory.INSTANCE;
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
        int i = 0;
        slotDefinition.addItemSlot(i++, 8, 5);
        for (int j = 0, size = results.size(); j < size; j++) {
            slotDefinition.addItemSlot(i++, j % 2 == 0 ? 126 : 145, 23 + (j / 2) * -19);
        }
        return i;
    }

    @Override
    protected int bindViewSlots(SlotFillContext slotFillContext) {
        int i = 0;
        slotFillContext.bindOptionalSlot(i++, ingredient, SLOT);
        Iterator<SlotContent> resultIterator = results.iterator();
        FloatListIterator chanceIterator = chances.iterator();
        while (resultIterator.hasNext()) {
            float chance = chanceIterator.nextFloat();
            if (chance == 1) {
                slotFillContext.bindOptionalSlot(i++, resultIterator.next(), SLOT);
            } else {
                bindChanceSlot(slotFillContext, i++, resultIterator.next(), chance);
            }
        }
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
        AllGuiTextures.JEI_ARROW.render(context, 78, 28);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 36, 0);
        AllGuiTextures.JEI_SHADOW.render(context, 25, 36);
        context.guiRenderState.addPicturesInPictureState(new MillstoneRenderState(
            new Matrix3x2f(context.pose()),
            35,
            15
        ));
    }
}
