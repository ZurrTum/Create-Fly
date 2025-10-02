package com.zurrtum.create.content.kinetics.belt.transport;

import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BeltCrusherInteractionHandler {
    public static boolean checkForCrushers(BeltInventory beltInventory, boolean isClient, TransportedItemStack currentItem, float nextOffset) {
        boolean beltMovementPositive = beltInventory.beltMovementPositive;
        int firstUpcomingSegment = (int) Math.floor(currentItem.beltPosition);
        int step = beltMovementPositive ? 1 : -1;
        firstUpcomingSegment = MathHelper.clamp(firstUpcomingSegment, 0, beltInventory.belt.beltLength - 1);

        for (int segment = firstUpcomingSegment; beltMovementPositive ? segment <= nextOffset : segment + 1 >= nextOffset; segment += step) {
            BlockPos crusherPos = BeltHelper.getPositionForOffset(beltInventory.belt, segment).up();
            World world = beltInventory.belt.getWorld();
            BlockState crusherState = world.getBlockState(crusherPos);
            if (!(crusherState.getBlock() instanceof CrushingWheelControllerBlock))
                continue;
            Direction crusherFacing = crusherState.get(CrushingWheelControllerBlock.FACING);
            Direction movementFacing = beltInventory.belt.getMovementFacing();
            if (crusherFacing != movementFacing)
                continue;

            float crusherEntry = segment + .5f;
            crusherEntry += .399f * (beltMovementPositive ? -1 : 1);
            float postCrusherEntry = crusherEntry + .799f * (!beltMovementPositive ? -1 : 1);

            boolean hasCrossed = nextOffset > crusherEntry && nextOffset < postCrusherEntry && beltMovementPositive || nextOffset < crusherEntry && nextOffset > postCrusherEntry && !beltMovementPositive;
            if (!hasCrossed)
                return false;
            currentItem.beltPosition = crusherEntry;

            if (isClient)
                return true;
            BlockEntity be = world.getBlockEntity(crusherPos);
            if (!(be instanceof CrushingWheelControllerBlockEntity crusherBE))
                return true;

            ItemStack toInsert = currentItem.stack;
            int count = toInsert.getCount();
            int insert = crusherBE.inventory.insert(toInsert);
            if (insert == 0) {
                return true;
            }
            if (insert == count) {
                currentItem.stack = ItemStack.EMPTY;
            } else {
                toInsert.setCount(count - insert);
            }
            beltInventory.belt.notifyUpdate();
            return true;
        }

        return false;
    }
}
