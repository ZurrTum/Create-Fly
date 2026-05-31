package com.zurrtum.create.client.compat.jei;

import com.google.common.base.Suppliers;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.compat.jei.renderer.JunkSlotRenderer;
import com.zurrtum.create.client.compat.jei.renderer.SlotRenderer;
import com.zurrtum.create.client.compat.jei.widget.ChanceTooltip;
import com.zurrtum.create.client.compat.jei.widget.KeepHeldTooltip;
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
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class CreateCategory<T> implements IRecipeCategory<T> {
    public static final SlotRenderer EMPTY = new SlotRenderer(null, 18, 18);
    public static final SlotRenderer SLOT = new SlotRenderer(AllGuiTextures.JEI_SLOT);
    public static final SlotRenderer CHANCE_SLOT = new SlotRenderer(AllGuiTextures.JEI_CHANCE_SLOT);
    public static final PotionTooltip POTION = new PotionTooltip();
    public static final KeepHeldTooltip KEEP_HELD = new KeepHeldTooltip();

    public static ContextMap createIngredientContext() {
        return SlotDisplayContext.fromLevel(Objects.requireNonNull(Minecraft.getInstance().level));
    }

    public static List<ItemStack> getStacks(SizedIngredient sizedIngredient, Supplier<ContextMap> context) {
        int count = sizedIngredient.getCount();
        Ingredient ingredient = sizedIngredient.getIngredient();
        if (count == 1) {
            return getStacks(ingredient, context);
        }
        CustomIngredient customIngredient = ((FabricIngredient) ingredient).getCustomIngredient();
        if (customIngredient == null) {
            return ingredient.values.stream().map(entry -> new ItemStack(entry, count)).toList();
        }
        return customIngredient.display().resolveForStacks(context.get()).stream()
            .map(stack -> stack.copyWithCount(stack.getCount() * count)).toList();
    }

    public static List<ItemStack> getStacks(Ingredient ingredient, Supplier<ContextMap> context) {
        CustomIngredient customIngredient = ((FabricIngredient) ingredient).getCustomIngredient();
        if (customIngredient == null) {
            return ingredient.values.stream().map(ItemStack::new).toList();
        }
        return customIngredient.display().resolveForStacks(context.get());
    }

    public static List<List<ItemStack>> condenseIngredients(List<Ingredient> ingredients) {
        List<ItemStack> cache = new ArrayList<>();
        List<List<ItemStack>> result = new ArrayList<>();
        Supplier<ContextMap> context = Suppliers.memoize(CreateCategory::createIngredientContext);
        Find:
        for (Ingredient ingredient : ingredients) {
            List<ItemStack> stacks = getStacks(ingredient, context);
            if (stacks.size() != 1) {
                result.add(stacks);
                continue;
            }
            ItemStack stack = stacks.getFirst();
            for (ItemStack target : cache) {
                if (ItemStack.isSameItemSameComponents(target, stack)) {
                    target.grow(1);
                    continue Find;
                }
            }
            stack = stack.copy();
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

    public static IRecipeSlotBuilder addFluidSlot(
        IRecipeLayoutBuilder builder,
        int x,
        int y,
        FluidIngredient fluidIngredient
    ) {
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
        IRecipeSlotBuilder slot = builder.addOutputSlot(x, y).add(output.create());
        float chance = output.chance();
        if (chance == 1) {
            slot.setBackground(SLOT, -1, -1);
        } else {
            slot.setBackground(CHANCE_SLOT, -1, -1).addRichTooltipCallback(new ChanceTooltip(chance));
        }
    }

    public static IRecipeSlotBuilder addJunkSlot(IRecipeLayoutBuilder builder, int x, int y) {
        return JunkSlotRenderer.addSlot(builder, x, y);
    }

    @Override
    public int getWidth() {
        return 177;
    }
}
