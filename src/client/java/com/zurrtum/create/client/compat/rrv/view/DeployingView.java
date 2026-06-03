package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.DeployingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.DeployerRenderState;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
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

public class DeployingView extends CreateView {
    private final Identifier id;
    private final List<SlotContent> results;
    private final FloatList chances;
    private final SlotContent target;
    private final SlotContent ingredient;
    private final boolean keepHeldItem;

    public DeployingView(Identifier id, ItemApplicationRecipe recipe) {
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
        target = SlotContent.of(recipe.target());
        ingredient = SlotContent.of(recipe.ingredient());
        keepHeldItem = recipe.keepHeldItem();
    }

    public DeployingView(Identifier id, SandPaperPolishingRecipe recipe) {
        this.id = id;
        results = List.of(SlotContent.of(recipe.result()));
        chances = FloatList.of(1);
        target = SlotContent.of(recipe.ingredient());
        ingredient = SlotContent.of(AllItemTags.SANDPAPER);
        keepHeldItem = true;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return DeployingCategory.INSTANCE;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(ingredient, target);
    }

    @Override
    public List<SlotContent> getResults() {
        return results;
    }

    @Override
    public int placeViewSlots(SlotDefinition slotDefinition) {
        int i = 0;
        slotDefinition.addItemSlot(i++, 51, 7);
        slotDefinition.addItemSlot(i++, 27, 53);
        int size = results.size();
        if (size == 1) {
            slotDefinition.addItemSlot(i++, 132, 53);
        } else {
            for (int j = 0; j < size; j++) {
                slotDefinition.addItemSlot(i++, j % 2 == 0 ? 122 : 141, 53 + (j / 2) * -19);
            }
        }
        return i;
    }

    @Override
    public int bindViewSlots(SlotFillContext slotFillContext) {
        int i = 0;
        if (keepHeldItem) {
            slotFillContext.addAdditionalStackModifier(i, NOT_CONSUMED);
        }
        slotFillContext.bindOptionalSlot(i++, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(i++, target, SLOT);
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
        AllGuiTextures.JEI_SHADOW.render(context, 62, 59);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 126, results.size() <= 2 ? 31 : 12);
        context.guiRenderState.addPicturesInPictureState(new DeployerRenderState(
            new Matrix3x2f(context.pose()),
            75,
            -8
        ));
    }
}
