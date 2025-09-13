package com.zurrtum.create.content.contraptions.behaviour.dispenser;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.dispenser.DefaultMountedDispenseBehavior;
import com.zurrtum.create.api.contraption.dispenser.MountedDispenseBehavior;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class DropperMovementBehaviour extends MovementBehaviour {
    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        if (context.world.isClient)
            return;

        MountedItemStorage storage = context.getItemStorage();
        if (storage == null)
            return;

        int slot = getSlot(storage, context.world.random, context.contraption.getStorage().getAllItems());
        if (slot == -1) {
            // all slots empty
            failDispense(context, pos);
            return;
        }

        // copy because dispense behaviors will modify it directly
        ItemStack stack = storage.getStack(slot).copy();
        MountedDispenseBehavior behavior = getDispenseBehavior(context, pos, stack);
        ItemStack remainder = behavior.dispense(stack, context, pos);
        storage.setStack(slot, remainder);
    }

    protected MountedDispenseBehavior getDispenseBehavior(MovementContext context, BlockPos pos, ItemStack stack) {
        return DefaultMountedDispenseBehavior.INSTANCE;
    }

    /**
     * Finds a dispensable slot. Empty slots are skipped and nearly-empty slots are topped off.
     */
    private static int getSlot(MountedItemStorage storage, Random random, Inventory contraptionInventory) {
        IntList filledSlots = new IntArrayList();
        for (int i = 0, size = storage.size(); i < size; i++) {
            ItemStack stack = storage.getStack(i);
            if (stack.isEmpty())
                continue;

            if (stack.getCount() == 1 && stack.getMaxCount() != 1) {
                stack = tryTopOff(stack, contraptionInventory);
                if (stack != null) {
                    storage.setStack(i, stack);
                } else {
                    continue;
                }
            }

            filledSlots.add(i);
        }

        return switch (filledSlots.size()) {
            case 0 -> -1;
            case 1 -> filledSlots.getInt(0);
            default -> Util.getRandom(filledSlots, random);
        };
    }

    @Nullable
    private static ItemStack tryTopOff(ItemStack stack, Inventory from) {
        int maxCount = stack.getMaxCount();
        int count = stack.getCount();
        ItemStack copy = stack.copy();
        copy.setCount(maxCount - count);
        int extract = from.extract(copy);
        if (extract == 0) {
            return null;
        }
        copy.setCount(count + extract);
        return copy;
    }

    private static void failDispense(MovementContext ctx, BlockPos pos) {
        ctx.world.syncWorldEvent(WorldEvents.DISPENSER_FAILS, pos, 0);
    }
}