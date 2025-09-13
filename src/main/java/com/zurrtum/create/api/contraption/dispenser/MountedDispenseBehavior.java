package com.zurrtum.create.api.contraption.dispenser;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.impl.contraption.dispenser.DispenserBehaviorConverter;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * A parallel to {@link DispenserBehavior}, for use by mounted dispensers.
 * Create will attempt to wrap existing {@link DispenserBehavior}s, but this interface can be used to provide better or fixed behavior.
 *
 * @see DefaultMountedDispenseBehavior
 * @see MountedProjectileDispenseBehavior
 * @see OptionalMountedDispenseBehavior
 */
@FunctionalInterface
public interface MountedDispenseBehavior {
    SimpleRegistry<Item, MountedDispenseBehavior> REGISTRY = Util.make(() -> {
        SimpleRegistry<Item, MountedDispenseBehavior> registry = SimpleRegistry.create();
        registry.registerProvider(DispenserBehaviorConverter.INSTANCE);
        return registry;
    });

    /**
     * Dispense the given stack into the world.
     *
     * @param stack   the stack to dispense. Safe to modify, behaviors are given a copy
     * @param context the MovementContext of the dispenser
     * @param pos     the BlockPos being visited by the dispenser
     * @return the remaining stack after dispensing one item
     */
    ItemStack dispense(ItemStack stack, MovementContext context, BlockPos pos);

    // utilities for implementations

    static Vec3d getDispenserNormal(MovementContext ctx) {
        Direction facing = ctx.state.get(DispenserBlock.FACING);
        Vec3d normal = Vec3d.of(facing.getVector());
        return ctx.rotation.apply(normal).normalize();
    }

    static Direction getClosestFacingDirection(Vec3d facing) {
        return Direction.getFacing(facing.x, facing.y, facing.z);
    }

    /**
     * Attempt to place an item back into the inventory. This is used in the case of item overflow, such as a stack
     * of buckets becoming two separate stacks when one is filled with water.
     * <p>
     * First tries to insert directly into the dispenser inventory. If that fails, it then tries the contraption's
     * whole inventory. If that still fails, the stack is dispensed into the world with the default behavior.
     *
     * @param stack   the stack to store in the inventory
     * @param context the MovementContext given to the behavior
     * @param pos     the position given to the behavior
     */
    static void placeItemInInventory(ItemStack stack, MovementContext context, BlockPos pos) {
        int count = stack.getCount();
        if (count == 0) {
            return;
        }
        ItemStack toInsert = stack.copy();
        // try inserting into own inventory first
        MountedItemStorage storage = context.getItemStorage();
        int insert;
        if (storage != null) {
            insert = storage.insert(toInsert);
            if (insert == count) {
                return;
            }
            if (insert > 0) {
                count -= insert;
                toInsert.setCount(count);
            }
        }
        // next, try the whole contraption inventory
        Inventory contraption = context.contraption.getStorage().getAllItems();
        insert = contraption.insert(toInsert);
        if (insert == count) {
            return;
        }
        if (insert > 0) {
            toInsert.setCount(count - insert);
        }
        // if there's *still* something left, dispense into world
        DefaultMountedDispenseBehavior.INSTANCE.dispense(toInsert, context, pos);
    }
}
