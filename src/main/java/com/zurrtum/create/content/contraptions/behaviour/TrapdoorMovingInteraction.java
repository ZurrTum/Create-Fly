package com.zurrtum.create.content.contraptions.behaviour;

import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class TrapdoorMovingInteraction extends SimpleBlockMovingInteraction {
    @Override
    protected BlockState handle(PlayerEntity player, Contraption contraption, BlockPos pos, BlockState currentState) {
        SoundEvent sound = currentState.get(TrapdoorBlock.OPEN) ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE : SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN;
        float pitch = player.getWorld().random.nextFloat() * 0.1F + 0.9F;
        playSound(player, sound, pitch);
        return currentState.cycle(TrapdoorBlock.OPEN);
    }

    @Override
    protected boolean updateColliders() {
        return true;
    }
}