package com.zurrtum.create.content.contraptions.actors.contraptionControls;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement.ElevatorFloorSelection;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.packet.c2s.ElevatorTargetFloorPacket;
import com.zurrtum.create.infrastructure.packet.s2c.ContraptionDisableActorPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Iterator;
import java.util.List;

public class ContraptionControlsMovingInteraction extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        Contraption contraption = contraptionEntity.getContraption();

        MutablePair<StructureBlockInfo, MovementContext> actor = contraption.getActorAt(localPos);
        if (actor == null)
            return false;
        MovementContext ctx = actor.right;
        if (ctx == null)
            return false;
        if (contraption instanceof ElevatorContraption ec)
            return elevatorInteraction(player, localPos, contraptionEntity, ec, ctx);
        if (contraptionEntity.getWorld().isClient()) {
            if (contraption.presentBlockEntities.get(ctx.localPos) instanceof ContraptionControlsBlockEntity cbe)
                cbe.pressButton();
            return true;
        }

        ItemStack filter = ContraptionControlsMovement.getFilter(ctx);
        boolean disable = true;
        boolean invert = false;

        List<ItemStack> disabledActors = contraption.getDisabledActors();
        for (Iterator<ItemStack> iterator = disabledActors.iterator(); iterator.hasNext(); ) {
            ItemStack presentFilter = iterator.next();
            boolean sameFilter = ContraptionControlsMovement.isSameFilter(presentFilter, filter);
            if (presentFilter.isEmpty()) {
                iterator.remove();
                disable = false;
                if (!sameFilter)
                    invert = true;
                continue;
            }
            if (!sameFilter)
                continue;
            iterator.remove();
            disable = false;
            break;
        }

        if (invert) {
            for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors()) {
                MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(pair.left.state());
                if (behaviour == null)
                    continue;
                ItemStack behaviourStack = behaviour.canBeDisabledVia(pair.right);
                if (behaviourStack == null)
                    continue;
                if (ContraptionControlsMovement.isSameFilter(behaviourStack, filter))
                    continue;
                if (contraption.isActorTypeDisabled(behaviourStack))
                    continue;
                disabledActors.add(behaviourStack);
                send(contraptionEntity, behaviourStack, true);
            }
        }

        if (filter.isEmpty())
            disabledActors.clear();
        if (disable)
            disabledActors.add(filter);

        contraption.setActorsActive(filter, !disable);
        ContraptionControlsBlockEntity.sendStatus(player, filter, !disable);
        send(contraptionEntity, filter, disable);

        AllSoundEvents.CONTROLLER_CLICK.play(
            player.getWorld(),
            null,
            BlockPos.ofFloored(contraptionEntity.toGlobalVector(Vec3d.ofCenter(localPos), 1)),
            1,
            disable ? 0.8f : 1.5f
        );

        if (!(contraptionEntity instanceof CarriageContraptionEntity cce))
            return true;
        if (!filter.isIn(ItemTags.DOORS))
            return true;

        // Special case: Doors are toggled on all carriages of a train
        Carriage carriage = cce.getCarriage();
        Train train = carriage.train;
        for (Carriage c : train.carriages) {
            CarriageContraptionEntity anyAvailableEntity = c.anyAvailableEntity();
            if (anyAvailableEntity == null)
                continue;
            Contraption cpt = anyAvailableEntity.getContraption();
            cpt.setActorsActive(filter, !disable);
            ContraptionControlsBlockEntity.sendStatus(player, filter, !disable);
            send(anyAvailableEntity, filter, disable);
        }

        return true;
    }

    private void send(AbstractContraptionEntity contraptionEntity, ItemStack filter, boolean disable) {
        ServerWorld world = (ServerWorld) contraptionEntity.getWorld();
        world.getChunkManager()
            .sendToOtherNearbyPlayers(contraptionEntity, new ContraptionDisableActorPacket(contraptionEntity.getId(), filter, !disable));
    }

    private boolean elevatorInteraction(
        PlayerEntity player,
        BlockPos localPos,
        AbstractContraptionEntity contraptionEntity,
        ElevatorContraption contraption,
        MovementContext ctx
    ) {
        World level = contraptionEntity.getWorld();
        if (!level.isClient()) {
            BlockPos pos = BlockPos.ofFloored(contraptionEntity.toGlobalVector(Vec3d.ofCenter(localPos), 1));
            AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, 1.5f);
            AllSoundEvents.CONTRAPTION_ASSEMBLE.play(level, null, pos, 0.75f, 0.8f);
            return true;
        }
        if (!(ctx.temporaryData instanceof ElevatorFloorSelection efs))
            return false;
        if (efs.currentTargetY == contraption.clientYTarget)
            return true;

        AllClientHandle.INSTANCE.sendPacket(player, new ElevatorTargetFloorPacket(contraptionEntity, efs.currentTargetY));
        if (contraption.presentBlockEntities.get(ctx.localPos) instanceof ContraptionControlsBlockEntity cbe)
            cbe.pressButton();
        return true;
    }

}
