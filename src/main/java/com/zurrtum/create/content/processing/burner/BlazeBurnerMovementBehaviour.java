package com.zurrtum.create.content.processing.burner;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.item.ItemStack;

public class BlazeBurnerMovementBehaviour extends MovementBehaviour {

    @Override
    public ItemStack canBeDisabledVia(MovementContext context) {
        return null;
    }

    @Override
    public void tick(MovementContext context) {
        if (!context.world.isClient())
            return;
        AllClientHandle.INSTANCE.tickBlazeBurnerMovement(context);
    }

    public void invalidate(MovementContext context) {
        context.data.remove("Conductor");
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
