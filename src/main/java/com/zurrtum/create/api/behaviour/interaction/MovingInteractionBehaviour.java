package com.zurrtum.create.api.behaviour.interaction;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
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
        if (contraptionEntity.getWorld().isClient) {
            AllClientHandle.INSTANCE.invalidateClientContraptionChildren(contraptionEntity.getContraption());
        }
    }

    protected void setContraptionBlockData(AbstractContraptionEntity contraptionEntity, BlockPos pos, StructureBlockInfo info) {
        if (contraptionEntity.getWorld().isClient())
            return;
        contraptionEntity.setBlock(pos, info);
    }

    public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        return true;
    }

    public void handleEntityCollision(Entity entity, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
    }
}
