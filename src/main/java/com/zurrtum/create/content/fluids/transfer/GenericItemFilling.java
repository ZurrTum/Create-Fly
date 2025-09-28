package com.zurrtum.create.content.fluids.transfer;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;

public class GenericItemFilling {
    public static boolean canItemBeFilled(World world, ItemStack stack) {
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

    public static int getRequiredAmountForItem(World world, ItemStack stack, FluidStack availableFluid) {
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
        if (fluid.matchesType(Fluids.WATER))
            return true;
        if (fluid.matchesType(AllFluids.POTION))
            return true;
        return fluid.matchesType(AllFluids.TEA);
    }

    public static ItemStack fillItem(World world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        FluidStack toFill = availableFluid.copy();
        toFill.setAmount(requiredAmount);

        if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(toFill)) {
            ItemStack fillBottle;
            Fluid fluid = toFill.getFluid();
            if (FluidHelper.isWater(fluid))
                fillBottle = PotionContentsComponent.createStack(Items.POTION, Potions.WATER);
            else if (fluid.matchesType(AllFluids.TEA))
                fillBottle = AllItems.BUILDERS_TEA.getDefaultStack();
            else
                fillBottle = PotionFluidHandler.fillBottle(stack, toFill);
            availableFluid.decrement(requiredAmount);
            stack.decrement(1);
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
            stack.decrement(1);
            return capability.getContainer();
        }
    }

}
