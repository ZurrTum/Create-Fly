package com.zurrtum.create.content.contraptions.behaviour.dispenser;

import com.zurrtum.create.api.contraption.dispenser.DefaultMountedDispenseBehavior;
import com.zurrtum.create.api.contraption.dispenser.MountedDispenseBehavior;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class DispenserMovementBehaviour extends DropperMovementBehaviour {
    @Override
    protected MountedDispenseBehavior getDispenseBehavior(MovementContext context, BlockPos pos, ItemStack stack) {
        MountedDispenseBehavior behavior = MountedDispenseBehavior.REGISTRY.get(stack.getItem());
        return behavior != null ? behavior : DefaultMountedDispenseBehavior.INSTANCE;
    }
}
