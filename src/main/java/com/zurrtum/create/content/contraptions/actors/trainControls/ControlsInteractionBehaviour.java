package com.zurrtum.create.content.contraptions.actors.trainControls;

import com.google.common.base.Objects;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class ControlsInteractionBehaviour extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        if (player.getItemInHand(activeHand).is(AllItems.WRENCH))
            return false;

        UUID currentlyControlling = contraptionEntity.getControllingPlayer().orElse(null);

        if (currentlyControlling != null) {
            contraptionEntity.stopControlling(localPos);
            if (Objects.equal(currentlyControlling, player.getUUID()))
                return true;
        }

        if (!contraptionEntity.startControlling(localPos, player))
            return false;

        contraptionEntity.setControllingPlayer(player);
        if (player.level().isClientSide())
            AllClientHandle.INSTANCE.startControlling(player, contraptionEntity, localPos);
        return true;
    }

}
