package com.zurrtum.create.client.compat.eiv.view;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.eiv.CreateView;
import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.eiv.display.MixingDisplay;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class MixingView extends CreateView {
    private final SlotContent result;
    private final List<SlotContent> ingredients;
    private final HeatCondition heat;
    private final SlotContent burner;
    private final SlotContent cake;


    public MixingView(MixingDisplay display) {
        result = SlotContent.of(display.result.isEmpty() ? getItemStack(display.fluidResult) : display.result);
        ingredients = new ArrayList<>(display.ingredients.size() + display.fluidIngredients.size());
        for (List<ItemStack> ingredient : display.ingredients) {
            ingredients.add(SlotContent.of(ingredient));
        }
        for (FluidIngredient ingredient : display.fluidIngredients) {
            ingredients.add(SlotContent.of(getItemStacks(ingredient)));
        }
        heat = display.heat;
        burner = heat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE) ? null : SlotContent.of(AllItems.BLAZE_BURNER);
        cake = heat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED) ? null : SlotContent.of(AllItems.BLAZE_CAKE);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return EivClientPlugin.MIXING;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return ingredients;
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(result);
    }

    @Override
    protected int placeViewSlots(SlotDefinition slotDefinition) {
        int i = 0;
        for (int size = ingredients.size(), xOffset = size < 3 ? 12 + (3 - size) * 19 / 2 : 12; i < size; i++) {
            slotDefinition.addItemSlot(i, xOffset + (i % 3) * 19, 51 - (i / 3) * 19);
        }
        slotDefinition.addItemSlot(i++, 142, 51);
        if (burner != null) {
            slotDefinition.addItemSlot(i++, 134, 81);
        }
        if (cake != null) {
            slotDefinition.addItemSlot(i++, 153, 81);
        }
        return i;
    }

    @Override
    protected int bindViewSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
        int i = 0;
        for (int size = ingredients.size(); i < size; i++) {
            slotFillContext.bindOptionalSlot(i, ingredients.get(i), SLOT);
        }
        slotFillContext.bindOptionalSlot(i++, result, SLOT);
        if (burner != null) {
            slotFillContext.bindSlot(i++, burner);
        }
        if (cake != null) {
            slotFillContext.bindSlot(i++, cake);
        }
        return i;
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition position, GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures.JEI_DOWN_ARROW.render(context, 136, 32);
        Matrix3x2f pose = new Matrix3x2f(context.pose());
        if (heat == HeatCondition.NONE) {
            AllGuiTextures.JEI_NO_HEAT_BAR.render(context, 0, 80);
            AllGuiTextures.JEI_SHADOW.render(context, 77, 68);
        } else {
            AllGuiTextures.JEI_HEAT_BAR.render(context, 0, 80);
            AllGuiTextures.JEI_LIGHT.render(context, 77, 88);
            context.guiRenderState.submitPicturesInPictureState(new BasinBlazeBurnerRenderState(pose, 87, 69, heat.visualizeAsBlazeBurner()));
        }
        context.guiRenderState.submitPicturesInPictureState(new MixingBasinRenderState(pose, 87, -5));
        context.drawString(Minecraft.getInstance().font, CreateLang.translateDirect(heat.getTranslationKey()), 5, 86, heat.getColor(), false);
    }
}
