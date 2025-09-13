package com.zurrtum.create.content.contraptions.actors.trainControls;

import com.google.common.base.Objects;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class ControlsInteractionBehaviour extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        if (player.getStackInHand(activeHand).isOf(AllItems.WRENCH))
            return false;

        UUID currentlyControlling = contraptionEntity.getControllingPlayer().orElse(null);

        if (currentlyControlling != null) {
            contraptionEntity.stopControlling(localPos);
            if (Objects.equal(currentlyControlling, player.getUuid()))
                return true;
        }

        if (!contraptionEntity.startControlling(localPos, player))
            return false;

        contraptionEntity.setControllingPlayer(player);
        if (player.getWorld().isClient)
            AllClientHandle.INSTANCE.startControlling(player, contraptionEntity, localPos);
        return true;
    }

}
