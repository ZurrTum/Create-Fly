package com.zurrtum.create.api.contraption.dispenser;

import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A mounted dispenser behavior that might fail, playing the empty sound if it does.
 */
public class OptionalMountedDispenseBehavior extends DefaultMountedDispenseBehavior {
    private boolean success;

    @Override
    protected final ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
        ItemStack remainder = this.doExecute(stack, context, pos, facing);
        this.success = remainder != null;
        return remainder == null ? stack : remainder;
    }

    @Override
    protected void playSound(LevelAccessor level, BlockPos pos) {
        if (this.success) {
            super.playSound(level, pos);
        } else {
            level.levelEvent(LevelEvent.SOUND_DISPENSER_FAIL, pos, 0);
        }
    }

    /**
     * Dispense the given item.
     *
     * @return the remaining items after dispensing one, or null if it failed
     */
    @Nullable
    protected ItemStack doExecute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
        return super.execute(stack, context, pos, facing);
    }
}
