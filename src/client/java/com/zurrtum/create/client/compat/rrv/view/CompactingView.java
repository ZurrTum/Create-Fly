package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.CompactingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CompactingView extends CreateView {
    private final Identifier id;
    private final List<SlotContent> results;
    private final FloatList chances;
    private final List<SlotContent> ingredients;
    private final HeatCondition heat;
    private final @Nullable SlotContent burner;
    private final @Nullable SlotContent cake;

    public CompactingView(Identifier id, CompactingRecipe recipe) {
        this.id = id;
        List<ProcessingOutput> results = recipe.results();
        int size = results.size();
        this.results = new ArrayList<>(size);
        chances = new FloatArrayList(size);
        for (ProcessingOutput result : results) {
            this.results.add(SlotContent.of(result.create()));
            chances.add(result.chance());
        }
        List<SizedIngredient> ingredients = recipe.ingredients();
        size = ingredients.size();
        List<FluidIngredient> fluidIngredients = recipe.fluidIngredients();
        int fluidSize = fluidIngredients.size();
        this.ingredients = new ArrayList<>(size + fluidSize);
        for (int i = 0; i < size; i++) {
            this.ingredients.add(createSlot(ingredients.get(i)));
        }
        for (int i = 0; i < fluidSize; i++) {
            this.ingredients.add(createSlot(fluidIngredients.get(i)));
        }
        heat = recipe.heat();
        burner = heat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE) ? null : SlotContent.of(AllItems.BLAZE_BURNER);
        cake = heat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED) ? null : SlotContent.of(AllItems.BLAZE_CAKE);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return CompactingCategory.INSTANCE;
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
        for (int size = ingredients.size(), xOffset = size < 3 ? 8 + (3 - size) * 19 / 2 : 8, yOffset =
             size <= 9 ? 51 : 60; i < size; i++) {
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
            float chance = chances.getFloat(j);
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
    public void renderRecipe(
        RecipeViewScreen screen,
        RecipePosition position,
        GuiGraphicsExtractor context,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 132, 32 - (results.size() - 1) / 2 * 19);
        Matrix3x2f pose = new Matrix3x2f(context.pose());
        if (heat == HeatCondition.NONE) {
            AllGuiTextures.JEI_NO_HEAT_BAR.render(context, 0, 80);
            AllGuiTextures.JEI_SHADOW.render(context, 77, 68);
        } else {
            AllGuiTextures.JEI_HEAT_BAR.render(context, 0, 80);
            AllGuiTextures.JEI_LIGHT.render(context, 77, 88);
            context.guiRenderState.addPicturesInPictureState(new BasinBlazeBurnerRenderState(
                pose,
                87,
                69,
                heat.visualizeAsBlazeBurner()
            ));
        }
        context.guiRenderState.addPicturesInPictureState(new PressBasinRenderState(pose, 87, -5));
        context.text(
            screen.getFont(),
            CreateLang.translateDirect(heat.getTranslationKey()),
            5,
            86,
            heat.getColor(),
            false
        );
    }
}
