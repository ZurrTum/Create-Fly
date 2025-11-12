package com.zurrtum.create.api.contraption.dispenser;

import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A parallel to {@link ProjectileDispenseBehavior}, providing a base implementation for projectile-shooting behaviors.
 */
public abstract class MountedProjectileDispenseBehavior extends DefaultMountedDispenseBehavior {
    @Override
    protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
        double x = pos.getX() + facing.x * .7 + .5;
        double y = pos.getY() + facing.y * .7 + .5;
        double z = pos.getZ() + facing.z * .7 + .5;
        Projectile projectile = getProjectile(context.world, x, y, z, stack.copy(), MountedDispenseBehavior.getClosestFacingDirection(facing));
        if (projectile == null)
            return stack;

        Vec3 motion = facing.scale(getPower()).add(context.motion);
        projectile.shoot(motion.x, motion.y, motion.z, (float) motion.length(), getUncertainty());
        context.world.addFreshEntity(projectile);
        stack.shrink(1);
        return stack;
    }

    @Override
    protected void playSound(LevelAccessor level, BlockPos pos) {
        level.levelEvent(LevelEvent.SOUND_DISPENSER_PROJECTILE_LAUNCH, pos, 0);
    }

    @Nullable
    protected abstract Projectile getProjectile(Level level, double x, double y, double z, ItemStack stack, Direction facing);

    protected float getUncertainty() {
        return 6;
    }

    protected float getPower() {
        return 1.1f;
    }

    /**
     * Create a mounted behavior wrapper from a vanilla projectile dispense behavior.
     */
    public static MountedDispenseBehavior of(ProjectileDispenseBehavior vanillaBehaviour) {
        return new MountedProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level level, double x, double y, double z, ItemStack stack, Direction facing) {
                return vanillaBehaviour.projectileItem.asProjectile(level, new Vec3(x, y, z), stack, facing);
            }

            @Override
            protected float getUncertainty() {
                return vanillaBehaviour.dispenseConfig.uncertainty();
            }

            @Override
            protected float getPower() {
                return vanillaBehaviour.dispenseConfig.power();
            }
        };
    }
}
