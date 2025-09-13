package com.zurrtum.create.api.contraption.dispenser;

import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

/**
 * A mounted dispenser behavior that might fail, playing the empty sound if it does.
 */
public class OptionalMountedDispenseBehavior extends DefaultMountedDispenseBehavior {
    private boolean success;

    @Override
    protected final ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
        ItemStack remainder = this.doExecute(stack, context, pos, facing);
        this.success = remainder != null;
        return remainder == null ? stack : remainder;
    }

    @Override
    protected void playSound(WorldAccess level, BlockPos pos) {
        if (this.success) {
            super.playSound(level, pos);
        } else {
            level.syncWorldEvent(WorldEvents.DISPENSER_FAILS, pos, 0);
        }
    }

    /**
     * Dispense the given item.
     *
     * @return the remaining items after dispensing one, or null if it failed
     */
    @Nullable
    protected ItemStack doExecute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
        return super.execute(stack, context, pos, facing);
    }
}
