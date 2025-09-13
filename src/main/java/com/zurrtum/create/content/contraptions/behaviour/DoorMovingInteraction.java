package com.zurrtum.create.content.contraptions.behaviour;

import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class DoorMovingInteraction extends SimpleBlockMovingInteraction {

    @Override
    protected BlockState handle(PlayerEntity player, Contraption contraption, BlockPos pos, BlockState currentState) {
        if (!(currentState.getBlock() instanceof DoorBlock))
            return currentState;

        boolean trainDoor = currentState.getBlock() instanceof SlidingDoorBlock;
        SoundEvent sound = currentState.get(DoorBlock.OPEN) ? trainDoor ? null : SoundEvents.BLOCK_WOODEN_DOOR_CLOSE : trainDoor ? SoundEvents.BLOCK_IRON_DOOR_OPEN : SoundEvents.BLOCK_WOODEN_DOOR_OPEN;

        BlockPos otherPos = currentState.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
        StructureBlockInfo info = contraption.getBlocks().get(otherPos);
        if (info != null && info.state().contains(DoorBlock.OPEN)) {
            BlockState newState = info.state().cycle(DoorBlock.OPEN);
            setContraptionBlockData(contraption.entity, otherPos, new StructureBlockInfo(info.pos(), newState, info.nbt()));
        }

        currentState = currentState.cycle(DoorBlock.OPEN);

        if (player != null) {

            if (trainDoor) {
                DoorHinge hinge = currentState.get(SlidingDoorBlock.HINGE);
                Direction facing = currentState.get(SlidingDoorBlock.FACING);
                BlockPos doublePos = pos.offset(hinge == DoorHinge.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
                StructureBlockInfo doubleInfo = contraption.getBlocks().get(doublePos);
                if (doubleInfo != null && SlidingDoorBlock.isDoubleDoor(currentState, hinge, facing, doubleInfo.state()))
                    handlePlayerInteraction(null, Hand.MAIN_HAND, doublePos, contraption.entity);
            }

            float pitch = player.getWorld().random.nextFloat() * 0.1F + 0.9F;
            if (sound != null)
                playSound(player, sound, pitch);
        }

        return currentState;
    }

    @Override
    protected boolean updateColliders() {
        return true;
    }

}
