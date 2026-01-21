package com.zurrtum.create.content.processing.burner;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class BlazeBurnerMovementBehaviour extends MovementBehaviour {

    @Override
    @Nullable
    public ItemStack canBeDisabledVia(MovementContext context) {
        return null;
    }

    @Override
    public void tick(MovementContext context) {
        if (!context.world.isClientSide())
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
