package com.zurrtum.create.content.decoration.slidingDoor;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class SlidingDoorBlockEntity extends SmartBlockEntity {

    public LerpedFloat animation;
    int bridgeTicks;
    boolean deferUpdate;

    public SlidingDoorBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SLIDING_DOOR, pos, state);
        animation = LerpedFloat.linear().startWithValue(isOpen(state) ? 1 : 0);
    }

    @Override
    public void tick() {
        if (deferUpdate && !world.isClient()) {
            deferUpdate = false;
            BlockState blockState = getCachedState();
            blockState.neighborUpdate(world, pos, Blocks.AIR, null, false);
        }

        super.tick();
        boolean open = isOpen(getCachedState());
        boolean wasSettled = animation.settled();
        animation.chase(open ? 1 : 0, .15f, Chaser.LINEAR);
        animation.tickChaser();

        if (world.isClient()) {
            if (bridgeTicks < 2 && open)
                bridgeTicks++;
            else if (bridgeTicks > 0 && !open && isVisible(getCachedState()))
                bridgeTicks--;
            return;
        }

        if (!open && !wasSettled && animation.settled() && !isVisible(getCachedState()))
            showBlockModel();
    }

    @Override
    protected Box createRenderBoundingBox() {
        return super.createRenderBoundingBox().expand(1);
    }

    protected boolean isVisible(BlockState state) {
        return state.get(SlidingDoorBlock.VISIBLE, true);
    }

    public boolean shouldRenderSpecial(BlockState state) {
        return !isVisible(state) || bridgeTicks != 0;
    }

    protected void showBlockModel() {
        world.setBlockState(pos, getCachedState().with(SlidingDoorBlock.VISIBLE, true), Block.NOTIFY_ALL);
        world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, .5f, 1);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    public static boolean isOpen(BlockState state) {
        return state.get(DoorBlock.OPEN, false);
    }

}
