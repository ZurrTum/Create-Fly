package com.zurrtum.create.content.contraptions.actors.seat;

import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class SeatInteractionBehaviour extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        return false;
    }

    @Override
    public void handleEntityCollision(Entity entity, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        Contraption contraption = contraptionEntity.getContraption();
        int index = contraption.getSeats().indexOf(localPos);
        if (index == -1)
            return;
        if (!SeatBlock.canBePickedUp(entity))
            return;
        if (entity instanceof Leashable leashable && leashable.isLeashed())
            return;
        contraptionEntity.addSittingPassenger(entity, index);
    }

}
