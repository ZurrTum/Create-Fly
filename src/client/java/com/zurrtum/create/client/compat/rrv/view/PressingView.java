package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.PressingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressRenderState;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
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

public class PressingView extends CreateView {
    private final Identifier id;
    private final List<SlotContent> results;
    private final FloatList chances;
    private final SlotContent ingredient;

    public PressingView(Identifier id, PressingRecipe recipe) {
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
        return PressingCategory.INSTANCE;
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
    public int placeViewSlots(SlotDefinition slotDefinition) {
        int i = 0;
        slotDefinition.addItemSlot(i++, 27, 55);
        for (int j = 0, size = results.size(); j < size; j++) {
            slotDefinition.addItemSlot(i++, 131 + 19 * j, 55);
        }
        return i;
    }

    @Override
    public int bindViewSlots(SlotFillContext slotFillContext) {
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
        AllGuiTextures.JEI_SHADOW.render(context, 61, 45);
        AllGuiTextures.JEI_LONG_ARROW.render(context, 52, 58);
        context.guiRenderState.addPicturesInPictureState(new PressRenderState(new Matrix3x2f(context.pose()), 73, -12));
    }
}
