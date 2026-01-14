package com.zurrtum.create.content.fluids.potion;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.BottleFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PotionFluidHandler {
    private static final Text NO_EFFECT = Text.translatable("effect.none").formatted(Formatting.GRAY);

    public static boolean isPotionItem(ItemStack stack) {
        return stack.getItem() instanceof PotionItem && !(stack.getItem().getRecipeRemainder().getItem() instanceof BucketItem) && !stack.isIn(
            AllItemTags.NOT_POTION);
    }

    public static Pair<FluidStack, ItemStack> emptyPotion(ItemStack stack, boolean simulate) {
        FluidStack fluid = getFluidFromPotionItem(stack);
        if (!simulate)
            stack.decrement(1);
        return Pair.of(fluid, new ItemStack(Items.GLASS_BOTTLE));
    }

    public static FluidStack getFluidFromPotionItem(ItemStack stack) {
        PotionContentsComponent potion = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        BottleType bottleTypeFromItem = bottleTypeFromItem(stack.getItem());
        if (potion.matches(Potions.WATER) && potion.customEffects().isEmpty() && bottleTypeFromItem == BottleType.REGULAR)
            return new FluidStack(Fluids.WATER, BottleFluidInventory.CAPACITY);
        FluidStack fluid = getFluidFromPotion(potion, bottleTypeFromItem, BottleFluidInventory.CAPACITY);
        fluid.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleTypeFromItem);
        return fluid;
    }

    public static FluidIngredient getFluidIngredientFromPotion(PotionContentsComponent potionContents, BottleType bottleType, int amount) {
        if (potionContents.matches(Potions.WATER) && bottleType == BottleType.REGULAR)
            return new FluidStackIngredient(Fluids.WATER, ComponentChanges.EMPTY, amount);
        return getFluidIngredient(amount, potionContents, bottleType);
    }

    public static FluidStack getFluidFromPotion(PotionContentsComponent potionContents, BottleType bottleType, int amount) {
        if (potionContents.matches(Potions.WATER) && bottleType == BottleType.REGULAR)
            return new FluidStack(Fluids.WATER, amount);
        return getFluidStack(amount, potionContents, bottleType);
    }

    public static FluidIngredient getFluidIngredient(int amount, PotionContentsComponent potionContents, BottleType bottleType) {
        ComponentChanges.Builder builder = ComponentChanges.builder();
        if (potionContents != PotionContentsComponent.DEFAULT) {
            builder.add(DataComponentTypes.POTION_CONTENTS, potionContents);
        }
        builder.add(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleType);
        return new FluidStackIngredient(AllFluids.POTION, builder.build(), amount);
    }

    public static FluidStack getFluidStack(int amount, PotionContentsComponent potionContents, BottleType bottleType) {
        FluidStack fluidStack = new FluidStack(AllFluids.POTION, amount);
        addPotionToFluidStack(fluidStack, potionContents);
        fluidStack.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleType);
        return fluidStack;
    }

    public static void addPotionToFluidStack(FluidStack fs, PotionContentsComponent potionContents) {
        if (potionContents == PotionContentsComponent.DEFAULT) {
            fs.remove(DataComponentTypes.POTION_CONTENTS);
            return;
        }
        fs.set(DataComponentTypes.POTION_CONTENTS, potionContents);
    }

    public static BottleType bottleTypeFromItem(Item item) {
        if (item == Items.LINGERING_POTION)
            return BottleType.LINGERING;
        if (item == Items.SPLASH_POTION)
            return BottleType.SPLASH;
        return BottleType.REGULAR;
    }

    public static Item itemFromBottleType(BottleType type) {
        return switch (type) {
            case LINGERING -> Items.LINGERING_POTION;
            case SPLASH -> Items.SPLASH_POTION;
            default -> Items.POTION;
        };
    }

    public static int getRequiredAmountForFilledBottle(ItemStack stack, FluidStack availableFluid) {
        return BottleFluidInventory.CAPACITY;
    }

    public static ItemStack fillBottle(ItemStack stack, FluidStack availableFluid) {
        ItemStack potionStack = new ItemStack(itemFromBottleType(availableFluid.getOrDefault(
            AllDataComponents.POTION_FLUID_BOTTLE_TYPE,
            BottleType.REGULAR
        )));
        potionStack.set(DataComponentTypes.POTION_CONTENTS, availableFluid.get(DataComponentTypes.POTION_CONTENTS));
        return potionStack;
    }
}
