package com.zurrtum.create.api.behaviour.interaction;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.schedule.Schedule;
import com.zurrtum.create.content.trains.schedule.ScheduleItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.function.Consumer;

/**
 * Partial interaction behavior implementation that allows blocks to act as conductors on trains, like Blaze Burners.
 */
public abstract class ConductorBlockInteractionBehavior extends MovingInteractionBehaviour {
    /**
     * Check if the given state is capable of being a conductor.
     */
    public abstract boolean isValidConductor(BlockState state);

    /**
     * Called when the conductor's schedule has changed.
     *
     * @param hasSchedule      true if the schedule was set, false if it was removed
     * @param blockStateSetter a consumer that will change the BlockState of this conductor on the contraption
     */
    protected void onScheduleUpdate(boolean hasSchedule, BlockState currentBlockState, Consumer<BlockState> blockStateSetter) {
    }

    @Override
    public final boolean handlePlayerInteraction(
        PlayerEntity player,
        Hand activeHand,
        BlockPos localPos,
        AbstractContraptionEntity contraptionEntity
    ) {
        if (!(contraptionEntity instanceof CarriageContraptionEntity carriageEntity))
            return false;
        if (activeHand == Hand.OFF_HAND)
            return false;
        Contraption contraption = carriageEntity.getContraption();
        if (!(contraption instanceof CarriageContraption carriageContraption))
            return false;

        StructureBlockInfo info = carriageContraption.getBlocks().get(localPos);
        if (info == null || !this.isValidConductor(info.state()))
            return false;

        Direction assemblyDirection = carriageContraption.getAssemblyDirection();
        ItemStack itemInHand = player.getStackInHand(activeHand);
        for (Direction direction : Iterate.directionsInAxis(assemblyDirection.getAxis())) {
            if (!carriageContraption.inControl(localPos, direction))
                continue;

            Train train = carriageEntity.getCarriage().train;
            if (train == null)
                return false;
            if (player.getWorld().isClient)
                return true;

            if (train.runtime.getSchedule() != null) {
                if (train.runtime.paused && !train.runtime.completed) {
                    train.runtime.paused = false;
                    AllSoundEvents.CONFIRM.playOnServer(player.getWorld(), player.getBlockPos(), 1, 1);
                    player.sendMessage(Text.translatable("create.schedule.continued"), true);
                    return true;
                }

                if (!itemInHand.isEmpty()) {
                    AllSoundEvents.DENY.playOnServer(player.getWorld(), player.getBlockPos(), 1, 1);
                    player.sendMessage(Text.translatable("create.schedule.remove_with_empty_hand"), true);
                    return true;
                }

                player.getWorld().playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.PLAYERS,
                    .2f,
                    1f + player.getWorld().random.nextFloat()
                );
                player.sendMessage(
                    Text.translatable(train.runtime.isAutoSchedule ? "create.schedule.auto_removed_from_train" : "create.schedule.removed_from_train"),
                    true
                );
                player.setStackInHand(activeHand, train.runtime.returnSchedule(player.getRegistryManager()));
                this.onScheduleUpdate(false, info.state(), newBlockState -> setBlockState(localPos, contraptionEntity, newBlockState));
                return true;
            }

            if (!itemInHand.isOf(AllItems.SCHEDULE))
                return true;

            Schedule schedule = ScheduleItem.getSchedule(player.getRegistryManager(), itemInHand);
            if (schedule == null)
                return false;

            if (schedule.entries.isEmpty()) {
                AllSoundEvents.DENY.playOnServer(player.getWorld(), player.getBlockPos(), 1, 1);
                player.sendMessage(Text.translatable("create.schedule.no_stops"), true);
                return true;
            }
            this.onScheduleUpdate(true, info.state(), newBlockState -> setBlockState(localPos, contraptionEntity, newBlockState));
            train.runtime.setSchedule(schedule, false);
            AllAdvancements.CONDUCTOR.trigger((ServerPlayerEntity) player);
            AllSoundEvents.CONFIRM.playOnServer(player.getWorld(), player.getBlockPos(), 1, 1);
            player.sendMessage(Text.translatable("create.schedule.applied_to_train").formatted(Formatting.GREEN), true);
            itemInHand.decrement(1);
            player.setStackInHand(activeHand, itemInHand.isEmpty() ? ItemStack.EMPTY : itemInHand);
            return true;
        }

        player.sendMessage(Text.translatable("create.schedule.non_controlling_seat"), true);
        AllSoundEvents.DENY.playOnServer(player.getWorld(), player.getBlockPos(), 1, 1);
        return true;
    }

    private void setBlockState(BlockPos localPos, AbstractContraptionEntity contraption, BlockState newState) {
        StructureBlockInfo info = contraption.getContraption().getBlocks().get(localPos);
        if (info != null) {
            setContraptionBlockData(contraption, localPos, new StructureBlockInfo(info.pos(), newState, info.nbt()));
        }
    }

    /**
     * Implementation used for Blaze Burners. May be reused by addons if applicable.
     */
    public static class BlazeBurner extends ConductorBlockInteractionBehavior {
        @Override
        public boolean isValidConductor(BlockState state) {
            return state.get(BlazeBurnerBlock.HEAT_LEVEL) != BlazeBurnerBlock.HeatLevel.NONE;
        }
    }
}
