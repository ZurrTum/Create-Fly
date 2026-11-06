package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.client.RecipeScreenContext;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.FanBlastingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import org.joml.Matrix3x2f;

import java.util.List;

public class FanBlastingView extends CreateView {
    private final Identifier id;
    private final SlotContent result;
    private final SlotContent ingredient;

    public FanBlastingView(Identifier id, ItemStackTemplate result, Ingredient ingredient) {
        this.id = id;
        this.result = SlotContent.of(result);
        this.ingredient = SlotContent.of(ingredient);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return FanBlastingCategory.INSTANCE;
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
        slotDefinition.addItemSlot(0, 17, 55);
        slotDefinition.addItemSlot(1, 137, 55);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, result, SLOT);
    }

    @Override
    public void renderRecipe(RecipeScreenContext context) {
        GuiGraphicsExtractor graphics = context.guiGraphics();
        AllGuiTextures.JEI_SHADOW.render(graphics, 42, 34);
        AllGuiTextures.JEI_LIGHT.render(graphics, 61, 46);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 50, 58);
        graphics.guiRenderState.addPicturesInPictureState(new FanRenderState(
            new Matrix3x2f(graphics.pose()),
            52,
            11,
            Fluids.LAVA.defaultFluidState().createLegacyBlock()
        ));
    }
}
