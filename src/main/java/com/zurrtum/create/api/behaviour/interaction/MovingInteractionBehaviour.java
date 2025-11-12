package com.zurrtum.create.api.behaviour.interaction;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.apache.commons.lang3.tuple.MutablePair;

/**
 * MovingInteractionBehaviors define behavior of blocks on contraptions
 * when interacted with by players or collided with by entities.
 */
public abstract class MovingInteractionBehaviour {
    public static final SimpleRegistry<Block, MovingInteractionBehaviour> REGISTRY = SimpleRegistry.create();

    protected void setContraptionActorData(AbstractContraptionEntity contraptionEntity, int index, StructureBlockInfo info, MovementContext ctx) {
        contraptionEntity.getContraption().getActors().remove(index);
        contraptionEntity.getContraption().getActors().add(index, MutablePair.of(info, ctx));
        if (contraptionEntity.level().isClientSide()) {
            AllClientHandle.INSTANCE.invalidateClientContraptionChildren(contraptionEntity.getContraption());
        }
    }

    protected void setContraptionBlockData(AbstractContraptionEntity contraptionEntity, BlockPos pos, StructureBlockInfo info) {
        if (contraptionEntity.level().isClientSide())
            return;
        contraptionEntity.setBlock(pos, info);
    }

    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        return true;
    }

    public void handleEntityCollision(Entity entity, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
    }
}
