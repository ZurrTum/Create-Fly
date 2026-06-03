package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.ManualApplicationCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.ManualBlockRenderState;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ManualApplicationView extends CreateView {
    private final Identifier id;
    private final List<SlotContent> results;
    private final FloatList chances;
    private final SlotContent target;
    private final SlotContent ingredient;
    private final boolean keepHeldItem;

    public ManualApplicationView(Identifier id, ManualApplicationRecipe recipe) {
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

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return ManualApplicationCategory.INSTANCE;
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
        slotDefinition.addItemSlot(i++, 51, 1);
        slotDefinition.addItemSlot(i++, 27, 34);
        int size = results.size();
        if (size == 1) {
            slotDefinition.addItemSlot(i++, 132, 34);
        } else {
            for (int j = 0; j < size; j++) {
                slotDefinition.addItemSlot(i++, j % 2 == 0 ? 128 : 147, 34 + (j / 2) * -19);
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
        AllGuiTextures.JEI_SHADOW.render(context, 67, 48);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 79, 11);
        ItemStack stack = target.getByIndex(target.index());
        if (stack.getItem() instanceof BlockItem blockItem) {
            BlockState block = blockItem.getBlock().defaultBlockState();
            context.guiRenderState.addPicturesInPictureState(new ManualBlockRenderState(
                new Matrix3x2f(context.pose()),
                block,
                79,
                30
            ));
        }
    }
}
