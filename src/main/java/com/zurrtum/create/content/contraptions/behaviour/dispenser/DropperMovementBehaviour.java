package com.zurrtum.create.content.contraptions.behaviour.dispenser;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.dispenser.DefaultMountedDispenseBehavior;
import com.zurrtum.create.api.contraption.dispenser.MountedDispenseBehavior;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;

public class DropperMovementBehaviour extends MovementBehaviour {
    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        if (context.world.isClientSide())
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
        ItemStack stack = storage.getItem(slot).copy();
        MountedDispenseBehavior behavior = getDispenseBehavior(context, pos, stack);
        ItemStack remainder = behavior.dispense(stack, context, pos);
        storage.setItem(slot, remainder);
    }

    protected MountedDispenseBehavior getDispenseBehavior(MovementContext context, BlockPos pos, ItemStack stack) {
        return DefaultMountedDispenseBehavior.INSTANCE;
    }

    /**
     * Finds a dispensable slot. Empty slots are skipped and nearly-empty slots are topped off.
     */
    private static int getSlot(MountedItemStorage storage, RandomSource random, Container contraptionInventory) {
        IntList filledSlots = new IntArrayList();
        for (int i = 0, size = storage.getContainerSize(); i < size; i++) {
            ItemStack stack = storage.getItem(i);
            if (stack.isEmpty())
                continue;

            if (stack.getCount() == 1 && stack.getMaxStackSize() != 1) {
                storage.setItem(i, ItemStack.EMPTY);
                boolean fill = tryTopOff(stack, contraptionInventory);
                storage.setItem(i, stack);
                if (!fill) {
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

    private static boolean tryTopOff(ItemStack stack, Container from) {
        int count = stack.getCount();
        int extract = from.extract(stack, stack.getMaxStackSize() - count);
        if (extract == 0) {
            return false;
        }
        stack.setCount(count + extract);
        return true;
    }

    private static void failDispense(MovementContext ctx, BlockPos pos) {
        ctx.world.levelEvent(LevelEvent.SOUND_DISPENSER_FAIL, pos, 0);
    }
}