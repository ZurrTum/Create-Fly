package com.zurrtum.create.content.contraptions.glue;

import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.catnip.data.Iterate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SuperGlueSelectionHelper {

    public static Set<BlockPos> searchGlueGroup(Level level, BlockPos startPos, BlockPos endPos, boolean includeOther) {
        if (endPos == null || startPos == null)
            return null;

        AABB bb = SuperGlueEntity.span(startPos, endPos);

        List<BlockPos> frontier = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> attached = new HashSet<>();
        Set<SuperGlueEntity> cachedOther = new HashSet<>();

        visited.add(startPos);
        frontier.add(startPos);

        while (!frontier.isEmpty()) {
            BlockPos currentPos = frontier.removeFirst();
            attached.add(currentPos);

            for (Direction d : Iterate.directions) {
                BlockPos offset = currentPos.relative(d);
                boolean gluePresent = includeOther && SuperGlueEntity.isGlued(level, currentPos, d, cachedOther);
                boolean alreadySticky = includeOther && SuperGlueEntity.isSideSticky(level, currentPos, d) || SuperGlueEntity.isSideSticky(
                    level,
                    offset,
                    d.getOpposite()
                );

                if (!alreadySticky && !gluePresent && !bb.contains(Vec3.atCenterOf(offset)))
                    continue;
                if (!BlockMovementChecks.isMovementNecessary(level.getBlockState(offset), level, offset))
                    continue;
                if (!SuperGlueEntity.isValidFace(level, currentPos, d) || !SuperGlueEntity.isValidFace(level, offset, d.getOpposite()))
                    continue;

                if (visited.add(offset))
                    frontier.add(offset);
            }
        }

        if (attached.size() < 2 && attached.contains(endPos))
            return null;

        return attached;
    }

    public static boolean collectGlueFromInventory(Player player, int requiredAmount, boolean simulate) {
        if (player.isCreative())
            return true;
        if (requiredAmount == 0)
            return true;

        NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
        for (int i = -1; i < Inventory.INVENTORY_SIZE; i++) {
            int slot = i == -1 ? player.getInventory().getSelectedSlot() : i;
            ItemStack stack = items.get(slot);
            if (stack.isEmpty())
                continue;
            if (!(stack.getItem() instanceof SuperGlueItem))
                continue;

            int charges = Math.min(requiredAmount, stack.getMaxDamage() - stack.getDamageValue());

            stack.hurtAndBreak(charges, player, EquipmentSlot.MAINHAND);

            requiredAmount -= charges;
            if (requiredAmount <= 0)
                return true;
        }

        return false;
    }

}
