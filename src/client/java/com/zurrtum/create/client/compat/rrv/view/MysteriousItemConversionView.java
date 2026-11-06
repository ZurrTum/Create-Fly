package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.client.RecipeScreenContext;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.MysteriousItemConversionCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.List;

public class MysteriousItemConversionView extends CreateView {
    private final Identifier id;
    private final SlotContent ingredient;
    private final SlotContent result;

    public MysteriousItemConversionView(Identifier id, Item ingredient, Item result) {
        this.id = id;
        this.ingredient = SlotContent.of(ingredient);
        this.result = SlotContent.of(result);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return MysteriousItemConversionCategory.INSTANCE;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(ingredient);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(result);
    }

    @Override
    public void placeSlots(SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 27, 12);
        slotDefinition.addItemSlot(1, 132, 12);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, result, SLOT);
    }

    @Override
    public void renderRecipe(RecipeScreenContext context) {
        GuiGraphicsExtractor graphics = context.guiGraphics();
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 15);
        AllGuiTextures.JEI_QUESTION_MARK.render(graphics, 77, 0);
    }
}
