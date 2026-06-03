package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.FanWashingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluids;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class FanWashingView extends CreateView {
    private final Identifier id;
    private final List<SlotContent> results;
    private final SlotContent ingredient;
    private final FloatList chances;

    public FanWashingView(Identifier id, SplashingRecipe recipe) {
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
        return FanWashingCategory.INSTANCE;
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
        int size = results.size();
        if (size == 1) {
            slotDefinition.addItemSlot(0, 17, 55);
            slotDefinition.addItemSlot(1, 137, 55);
            return 2;
        } else {
            int xOffsetAmount = 1 - Math.min(3, size);
            slotDefinition.addItemSlot(0, 17 + xOffsetAmount * 5, 55);
            for (int i = 0, left = (size == 2 ? 137 : 132) + xOffsetAmount * 9, top = 55; i < size; i++) {
                slotDefinition.addItemSlot(i + 1, left + (i % 3) * 19, top + (i / 3) * -19);
            }
            return size + 1;
        }
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
        int xOffsetAmount = 1 - Math.min(3, results.size());
        AllGuiTextures.JEI_SHADOW.render(context, 42, 34);
        AllGuiTextures.JEI_LIGHT.render(context, 61, 46);
        AllGuiTextures.JEI_LONG_ARROW.render(context, 50 + 7 * xOffsetAmount, 58);
        context.guiRenderState.addPicturesInPictureState(new FanRenderState(
            new Matrix3x2f(context.pose()),
            52,
            11,
            Fluids.WATER.defaultFluidState().createLegacyBlock()
        ));
    }
}
