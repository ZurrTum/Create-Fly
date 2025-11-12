package com.zurrtum.create.content.fluids.transfer;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class GenericItemFilling {
    public static boolean canItemBeFilled(Level world, ItemStack stack) {
        if (stack.getItem() == Items.GLASS_BOTTLE)
            return true;
        if (stack.getItem() == Items.MILK_BUCKET)
            return false;

        try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack)) {
            if (capability == null) {
                return false;
            }
            for (int i = 0, size = capability.size(); i < size; i++) {
                FluidStack fluidStack = capability.getStack(i);
                if (fluidStack.getAmount() < capability.getMaxAmount(fluidStack)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
        if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(availableFluid))
            return PotionFluidHandler.getRequiredAmountForFilledBottle(stack, availableFluid);

        try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack)) {
            if (capability == null) {
                return -1;
            }
            return capability.countSpace(availableFluid);
        }
    }

    private static boolean canFillGlassBottleInternally(FluidStack availableFluid) {
        Fluid fluid = availableFluid.getFluid();
        if (fluid.isSame(Fluids.WATER))
            return true;
        if (fluid.isSame(AllFluids.POTION))
            return true;
        return fluid.isSame(AllFluids.TEA);
    }

    public static ItemStack fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        FluidStack toFill = availableFluid.copy();
        toFill.setAmount(requiredAmount);

        if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(toFill)) {
            ItemStack fillBottle;
            Fluid fluid = toFill.getFluid();
            if (FluidHelper.isWater(fluid))
                fillBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
            else if (fluid.isSame(AllFluids.TEA))
                fillBottle = AllItems.BUILDERS_TEA.getDefaultInstance();
            else
                fillBottle = PotionFluidHandler.fillBottle(stack, toFill);
            availableFluid.decrement(requiredAmount);
            stack.shrink(1);
            return fillBottle;
        }

        try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack.copyWithCount(1))) {
            if (capability == null) {
                return ItemStack.EMPTY;
            }
            int insert = capability.insert(toFill);
            if (insert == 0) {
                return ItemStack.EMPTY;
            }
            availableFluid.decrement(insert);
            stack.shrink(1);
            return capability.getContainer();
        }
    }

}
