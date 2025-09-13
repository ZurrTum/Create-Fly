package com.zurrtum.create.impl.unpacking;

import com.zurrtum.create.AllUnpackingHandlers;
import com.zurrtum.create.api.packager.unpacking.UnpackingHandler;
import com.zurrtum.create.content.kinetics.crafter.ConnectedInputHandler.ConnectedInput;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.CrafterItemHandler;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrafterUnpackingHandler implements UnpackingHandler {
    @Override
    public boolean unpack(
        World level,
        BlockPos pos,
        BlockState state,
        Direction side,
        List<ItemStack> items,
        @Nullable PackageOrderWithCrafts orderContext,
        boolean simulate
    ) {
        if (!PackageOrderWithCrafts.hasCraftingInformation(orderContext))
            return AllUnpackingHandlers.DEFAULT.unpack(level, pos, state, side, items, null, simulate);

        // Get item placement
        List<BigItemStack> craftingContext = orderContext.getCraftingInformation();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MechanicalCrafterBlockEntity crafter))
            return false;

        ConnectedInput input = crafter.getInput();
        CrafterItemHandler[] inventories = input.getInventories(level, pos);
        int size = inventories.length;
        if (size == 0)
            return false;

        // insert in the order's defined ordering
        int max = Math.min(size, craftingContext.size());
        outer:
        for (int i = 0; i < max; i++) {
            BigItemStack targetStack = craftingContext.get(i);
            if (targetStack.stack.isEmpty())
                continue;

            CrafterItemHandler inventory = inventories[i];
            // if there's already an item here, no point in trying
            if (!inventory.getStack().isEmpty())
                continue;

            // go through each item in the box and try insert if it matches the target
            for (ItemStack stack : items) {
                if (ItemStack.areItemsAndComponentsEqual(stack, targetStack.stack)) {
                    int insert;
                    if (simulate) {
                        insert = inventory.countSpace(stack, 1);
                    } else {
                        insert = inventory.insert(stack, 1);
                    }
                    if (insert == 1) {
                        stack.decrement(1);
                        // one item per crafter, move to next once successful
                        continue outer;
                    }
                }
            }
        }

        // if anything is still non-empty insertion failed
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                return false;
            }
        }

        if (!simulate) {
            crafter.checkCompletedRecipe(true);
        }

        return true;
    }
}
