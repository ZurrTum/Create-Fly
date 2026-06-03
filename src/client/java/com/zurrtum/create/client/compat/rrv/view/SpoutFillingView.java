package com.zurrtum.create.client.compat.rrv.view;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import cc.cassian.rrv.common.recipe.item.FluidItem;
import com.zurrtum.create.client.compat.rrv.CreateView;
import com.zurrtum.create.client.compat.rrv.category.SpoutFillingCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SpoutRenderState;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SpoutFillingView extends CreateView {
    public static final int MAX = 3;
    public static AtomicInteger idGenerator = new AtomicInteger();
    private final Identifier id;
    private final SlotContent result;
    private final SlotContent fluidIngredient;
    private final SlotContent ingredient;

    public SpoutFillingView(Identifier id, FillingRecipe recipe) {
        this.id = id;
        result = SlotContent.of(recipe.result());
        fluidIngredient = createSlot(recipe.fluidIngredient());
        ingredient = SlotContent.of(recipe.ingredient());
    }

    public SpoutFillingView(Identifier id, ItemStack result, FluidStack fluidIngredient, ItemStack ingredient) {
        this.id = id;
        this.result = SlotContent.of(result);
        this.fluidIngredient = createSlot(fluidIngredient);
        this.ingredient = SlotContent.of(ingredient);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return SpoutFillingCategory.INSTANCE;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(ingredient, fluidIngredient);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(result);
    }

    @Override
    public void placeSlots(SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 27, 49);
        slotDefinition.addItemSlot(1, 27, 30);
        slotDefinition.addItemSlot(2, 132, 49);
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, ingredient, SLOT);
        slotFillContext.bindOptionalSlot(1, fluidIngredient, SLOT);
        slotFillContext.bindOptionalSlot(2, result, SLOT);
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
        AllGuiTextures.JEI_SHADOW.render(context, 62, 55);
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 126, 27);
        ItemStack stack = fluidIngredient.getByIndex(fluidIngredient.index());
        if (stack.getItem() instanceof FluidItem item) {
            int i = idGenerator.getAndIncrement();
            if (i >= MAX) {
                idGenerator.set(0);
            }
            context.guiRenderState.addPicturesInPictureState(new SpoutRenderState(
                i,
                new Matrix3x2f(context.pose()),
                item.getFluid(),
                stack.getComponentsPatch(),
                75,
                -1,
                0
            ));
        }
    }
}
