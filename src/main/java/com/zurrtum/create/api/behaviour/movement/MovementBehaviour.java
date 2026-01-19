package com.zurrtum.create.api.behaviour.movement;

import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * MovementBehaviors, also known as Actors, provide behavior to blocks mounted on contraptions.
 * Blocks may be associated with a behavior through {@link #REGISTRY}.
 */
public abstract class MovementBehaviour {
    public static final SimpleRegistry<Block, MovementBehaviour> REGISTRY = SimpleRegistry.create();
    public Object attachRender;

    @SuppressWarnings("unchecked")
    public <T> T getAttachRender() {
        return (T) attachRender;
    }

    public boolean isActive(MovementContext context) {
        return !context.disabled;
    }

    public void tick(MovementContext context) {
    }

    public void startMoving(MovementContext context) {
    }

    public void visitNewPosition(MovementContext context, BlockPos pos) {
    }

    public Vec3 getActiveAreaOffset(MovementContext context) {
        return Vec3.ZERO;
    }

    @Nullable
    public ItemStack canBeDisabledVia(MovementContext context) {
        Block block = context.state.getBlock();
        if (block == null)
            return null;
        return new ItemStack(block);
    }

    public void onDisabledByControls(MovementContext context) {
        cancelStall(context);
    }

    public boolean mustTickWhileDisabled() {
        return false;
    }

    /**
     * @deprecated since 6.0.9 - use {@link MovementBehaviour#collectOrDropItem(MovementContext, ItemStack)} instead.
     * No behaviours altered, simply a rename to reflect that we do collect items when
     * applicable before considering dropping the remainder into the world.
     */
    @Deprecated(since = "6.0.9", forRemoval = true)
    public void dropItem(MovementContext context, ItemStack stack) {
        collectOrDropItem(context, stack);
    }

    public void collectOrDropItem(MovementContext context, ItemStack stack) {
        if (AllConfigs.server().kinetics.moveItemsToStorage.get()) {
            int insert = context.contraption.getStorage().getAllItems().insert(stack);
            if (insert > 0) {
                int count = stack.getCount();
                if (insert == count) {
                    stack = ItemStack.EMPTY;
                } else {
                    stack.setCount(count);
                }
            }
        }
        if (stack.isEmpty())
            return;

        // Actors might void items if their positions is undefined
        Vec3 vec = context.position;
        if (vec == null)
            return;

        ItemEntity itemEntity = new ItemEntity(context.world, vec.x, vec.y, vec.z, stack);
        itemEntity.setDeltaMovement(context.motion.add(0, 0.5f, 0).scale(context.world.getRandom().nextFloat() * .3f));
        context.world.addFreshEntity(itemEntity);
    }

    public void onSpeedChanged(MovementContext context, Vec3 oldMotion, Vec3 motion) {
    }

    public void stopMoving(MovementContext context) {
    }

    public void cancelStall(MovementContext context) {
        context.stall = false;
    }

    public void writeExtraData(MovementContext context) {
    }

    public boolean disableBlockEntityRendering() {
        return false;
    }
}
