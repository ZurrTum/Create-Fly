package com.zurrtum.create.content.contraptions.behaviour;

import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public abstract class SimpleBlockMovingInteraction extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        Contraption contraption = contraptionEntity.getContraption();
        StructureBlockInfo info = contraption.getBlocks().get(localPos);

        BlockState newState = handle(player, contraption, localPos, info.state());
        if (info.state() == newState)
            return false;

        setContraptionBlockData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), newState, info.nbt()));
        if (updateColliders())
            contraption.invalidateColliders();
        return true;
    }

    protected boolean updateColliders() {
        return false;
    }

    protected void playSound(PlayerEntity player, SoundEvent sound, float pitch) {
        player.getEntityWorld().playSound(player, player.getBlockPos(), sound, SoundCategory.BLOCKS, 0.3f, pitch);
    }

    protected abstract BlockState handle(PlayerEntity player, Contraption contraption, BlockPos pos, BlockState currentState);

}
