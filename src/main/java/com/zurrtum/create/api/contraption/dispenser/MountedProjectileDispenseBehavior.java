package com.zurrtum.create.api.contraption.dispenser;

import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

/**
 * A parallel to {@link ProjectileDispenserBehavior}, providing a base implementation for projectile-shooting behaviors.
 */
public abstract class MountedProjectileDispenseBehavior extends DefaultMountedDispenseBehavior {
    @Override
    protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
        double x = pos.getX() + facing.x * .7 + .5;
        double y = pos.getY() + facing.y * .7 + .5;
        double z = pos.getZ() + facing.z * .7 + .5;
        ProjectileEntity projectile = getProjectile(context.world, x, y, z, stack.copy(), MountedDispenseBehavior.getClosestFacingDirection(facing));
        if (projectile == null)
            return stack;

        Vec3d motion = facing.multiply(getPower()).add(context.motion);
        projectile.setVelocity(motion.x, motion.y, motion.z, (float) motion.length(), getUncertainty());
        context.world.spawnEntity(projectile);
        stack.decrement(1);
        return stack;
    }

    @Override
    protected void playSound(WorldAccess level, BlockPos pos) {
        level.syncWorldEvent(WorldEvents.DISPENSER_LAUNCHES_PROJECTILE, pos, 0);
    }

    @Nullable
    protected abstract ProjectileEntity getProjectile(World level, double x, double y, double z, ItemStack stack, Direction facing);

    protected float getUncertainty() {
        return 6;
    }

    protected float getPower() {
        return 1.1f;
    }

    /**
     * Create a mounted behavior wrapper from a vanilla projectile dispense behavior.
     */
    public static MountedDispenseBehavior of(ProjectileDispenserBehavior vanillaBehaviour) {
        return new MountedProjectileDispenseBehavior() {
            @Override
            protected ProjectileEntity getProjectile(World level, double x, double y, double z, ItemStack stack, Direction facing) {
                return vanillaBehaviour.projectile.createEntity(level, new Vec3d(x, y, z), stack, facing);
            }

            @Override
            protected float getUncertainty() {
                return vanillaBehaviour.projectileSettings.uncertainty();
            }

            @Override
            protected float getPower() {
                return vanillaBehaviour.projectileSettings.power();
            }
        };
    }
}
