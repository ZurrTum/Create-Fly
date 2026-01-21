package com.zurrtum.create.content.trains.schedule;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ScheduleItemEntityInteraction {
    @Nullable
    public static InteractionResult interactWithConductor(Entity entity, Player player, InteractionHand hand) {
        Entity rootVehicle = entity.getRootVehicle();
        if (!(rootVehicle instanceof CarriageContraptionEntity cce))
            return null;
        if (!(entity instanceof LivingEntity living))
            return null;
        ItemStack schedule = AllItems.SCHEDULE.getDefaultInstance();
        if (player.getCooldowns().isOnCooldown(schedule))
            return null;

        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.getItem() instanceof ScheduleItem si) {
            InteractionResult result = si.handScheduleTo(itemStack, player, living, hand);
            if (result.consumesAction()) {
                player.getCooldowns().addCooldown(schedule, 5);
                return result;
            }
        }

        if (hand == InteractionHand.OFF_HAND)
            return null;

        Contraption contraption = cce.getContraption();
        if (!(contraption instanceof CarriageContraption cc))
            return null;

        Train train = cce.getCarriage().train;
        if (train == null)
            return null;
        if (train.runtime.getSchedule() == null)
            return null;

        Integer seatIndex = contraption.getSeatMapping().get(entity.getUUID());
        if (seatIndex == null)
            return null;
        BlockPos seatPos = contraption.getSeats().get(seatIndex);
        Couple<Boolean> directions = cc.conductorSeats.get(seatPos);
        if (directions == null)
            return null;

        Level world = player.level();
        boolean onServer = !world.isClientSide();

        if (train.runtime.paused && !train.runtime.completed) {
            if (onServer) {
                train.runtime.paused = false;
                AllSoundEvents.CONFIRM.playOnServer(world, player.blockPosition(), 1, 1);
                player.displayClientMessage(Component.translatable("create.schedule.continued"), true);
            }

            player.getCooldowns().addCooldown(schedule, 5);
            return InteractionResult.SUCCESS;
        }

        if (!itemStack.isEmpty()) {
            if (onServer) {
                AllSoundEvents.DENY.playOnServer(world, player.blockPosition(), 1, 1);
                player.displayClientMessage(Component.translatable("create.schedule.remove_with_empty_hand"), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (onServer) {
            world.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f, 1f + world.getRandom().nextFloat());
            player.displayClientMessage(
                Component.translatable(train.runtime.isAutoSchedule ? "create.schedule.auto_removed_from_train" : "create.schedule.removed_from_train"),
                true
            );

            player.getInventory().placeItemBackInInventory(train.runtime.returnSchedule(player.registryAccess()));
        }

        player.getCooldowns().addCooldown(schedule, 5);
        return InteractionResult.SUCCESS;
    }
}