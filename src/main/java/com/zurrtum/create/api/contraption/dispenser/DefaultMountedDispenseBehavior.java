package com.zurrtum.create.api.contraption.dispenser;

import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;

/**
 * A parallel to {@link ItemDispenserBehavior}, providing a common, default, extendable dispense implementation.
 */
public class DefaultMountedDispenseBehavior implements MountedDispenseBehavior {
    /**
     * A reusable instance of the default behavior.
     */
    public static final MountedDispenseBehavior INSTANCE = new DefaultMountedDispenseBehavior();

    @Override
    public ItemStack dispense(ItemStack stack, MovementContext context, BlockPos pos) {
        Vec3d normal = MountedDispenseBehavior.getDispenserNormal(context);

        Direction closestToFacing = MountedDispenseBehavior.getClosestFacingDirection(normal);
        Inventory inventory = HopperBlockEntity.getInventoryAt(context.world, pos.offset(closestToFacing));
        if (inventory == null) {
            ItemStack remainder = this.execute(stack, context, pos, normal);
            this.playSound(context.world, pos);
            this.playAnimation(context.world, pos, closestToFacing);
            return remainder;
        } else {
            ItemStack toInsert = stack.copyWithCount(1);
            ItemStack remainder = HopperBlockEntity.transfer(null, inventory, toInsert, closestToFacing.getOpposite());
            if (remainder.isEmpty()) {
                stack.decrement(1);
            }
        }
        return stack;
    }

    /**
     * Dispense the given item. Sounds and particles are already handled.
     *
     * @return the remaining items after dispensing one
     */
    protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
        ItemStack toDispense = stack.split(1);
        spawnItem(context.world, toDispense, 6, facing, pos, context);
        return stack;
    }

    protected void playSound(WorldAccess level, BlockPos pos) {
        level.syncWorldEvent(WorldEvents.DISPENSER_DISPENSES, pos, 0);
    }

    protected void playAnimation(WorldAccess level, BlockPos pos, Vec3d facing) {
        this.playAnimation(level, pos, MountedDispenseBehavior.getClosestFacingDirection(facing));
    }

    protected void playAnimation(WorldAccess level, BlockPos pos, Direction direction) {
        level.syncWorldEvent(WorldEvents.DISPENSER_ACTIVATED, pos, direction.getIndex());
    }

    public static void spawnItem(World level, ItemStack stack, int speed, Vec3d facing, BlockPos pos, MovementContext context) {
        double x = pos.getX() + facing.x + .5;
        double y = pos.getY() + facing.y + .5;
        double z = pos.getZ() + facing.z + .5;
        if (MountedDispenseBehavior.getClosestFacingDirection(facing).getAxis() == Direction.Axis.Y) {
            y = y - 0.125;
        } else {
            y = y - 0.15625;
        }

        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        double d3 = level.random.nextDouble() * 0.1 + 0.2;
        entity.setVelocity(
            level.random.nextGaussian() * 0.0075 * speed + facing.getX() * d3 + context.motion.x,
            level.random.nextGaussian() * 0.0075 * speed + facing.getY() * d3 + context.motion.y,
            level.random.nextGaussian() * 0.0075 * speed + facing.getZ() * d3 + context.motion.z
        );
        level.spawnEntity(entity);
    }
}
