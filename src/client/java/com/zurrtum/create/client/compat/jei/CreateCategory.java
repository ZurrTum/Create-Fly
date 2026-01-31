package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.compat.jei.renderer.JunkSlotRenderer;
import com.zurrtum.create.client.compat.jei.renderer.SlotRenderer;
import com.zurrtum.create.client.compat.jei.widget.ChanceTooltip;
import com.zurrtum.create.client.compat.jei.widget.PotionTooltip;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;

public abstract class CreateCategory<T> implements IRecipeCategory<T> {
    public static final SlotRenderer EMPTY = new SlotRenderer(null, 18, 18);
    public static final SlotRenderer SLOT = new SlotRenderer(AllGuiTextures.JEI_SLOT);
    public static final SlotRenderer CHANCE_SLOT = new SlotRenderer(AllGuiTextures.JEI_CHANCE_SLOT);
    public static final PotionTooltip POTION = new PotionTooltip();

    public static List<ItemStack> getStacks(SizedIngredient ingredient) {
        int count = ingredient.getCount();
        return ingredient.getIngredient().values.stream().map(entry -> new ItemStack(entry, count)).toList();
    }

    public static List<ItemStack> getStacks(Ingredient ingredient) {
        return ingredient.values.stream().map(ItemStack::new).toList();
    }

    public static List<List<ItemStack>> condenseIngredients(List<Ingredient> ingredients) {
        List<ItemStack> cache = new ArrayList<>();
        List<List<ItemStack>> result = new ArrayList<>();
        Find:
        for (Ingredient ingredient : ingredients) {
            HolderSet<Item> entries = ingredient.values;
            if (entries.size() != 1) {
                result.add(getStacks(ingredient));
                continue;
            }
            Item item = entries.get(0).value();
            for (ItemStack target : cache) {
                if (target.is(item)) {
                    target.grow(1);
                    continue Find;
                }
            }
            ItemStack stack = item.getDefaultInstance();
            cache.add(stack);
            result.add(List.of(stack));
        }
        return result;
    }

    public static IRecipeSlotBuilder addFluidSlot(IRecipeLayoutBuilder builder, int x, int y, FluidStack stack) {
        int amount = stack.getAmount();
        IRecipeSlotBuilder slot = builder.addOutputSlot(x, y).setFluidRenderer(amount, false, 16, 16)
            .add(stack.getFluid(), amount, stack.getComponentChanges());
        if (stack.isOf(AllFluids.POTION)) {
            slot.addRichTooltipCallback(POTION);
        }
        return slot;
    }

    public static IRecipeSlotBuilder addFluidSlot(IRecipeLayoutBuilder builder, int x, int y, FluidIngredient fluidIngredient) {
        int amount = fluidIngredient.amount();
        IRecipeSlotBuilder slot = builder.addInputSlot(x, y).setFluidRenderer(amount, false, 16, 16);
        DataComponentPatch components = DataComponentPatch.EMPTY;
        if (fluidIngredient instanceof FluidStackIngredient stackIngredient) {
            components = stackIngredient.components();
        }
        boolean ignorePotion = true;
        for (Fluid fluid : fluidIngredient.getMatchingFluids()) {
            slot.add(fluid, amount, components);
            if (ignorePotion && fluid == AllFluids.POTION) {
                ignorePotion = false;
            }
        }
        if (ignorePotion) {
            return slot;
        }
        return slot.addRichTooltipCallback(POTION);
    }

    public static void addChanceSlot(IRecipeLayoutBuilder builder, int x, int y, ProcessingOutput output) {
        IRecipeSlotBuilder slot = builder.addOutputSlot(x, y).add(output.stack());
        if (output.chance() == 1) {
            slot.setBackground(SLOT, -1, -1);
        } else {
            slot.setBackground(CHANCE_SLOT, -1, -1).addRichTooltipCallback(new ChanceTooltip(output));
        }
    }

    public static void addJunkSlot(IRecipeLayoutBuilder builder, int x, int y, float chance) {
        JunkSlotRenderer.addSlot(builder, x, y, chance);
    }

    @Override
    public int getWidth() {
        return 177;
    }
}
