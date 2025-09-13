package com.zurrtum.create.content.contraptions.behaviour;

import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class LeverMovingInteraction extends SimpleBlockMovingInteraction {
    @Override
    protected BlockState handle(PlayerEntity player, Contraption contraption, BlockPos pos, BlockState currentState) {
        playSound(player, SoundEvents.BLOCK_LEVER_CLICK, currentState.get(LeverBlock.POWERED) ? 0.5f : 0.6f);
        return currentState.cycle(LeverBlock.POWERED);
    }
}