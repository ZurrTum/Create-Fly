package com.zurrtum.create.content.trains.schedule;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ScheduleItemEntityInteraction {
    public static ActionResult interactWithConductor(Entity entity, PlayerEntity player, Hand hand) {
        Entity rootVehicle = entity.getRootVehicle();
        if (!(rootVehicle instanceof CarriageContraptionEntity cce))
            return null;
        if (!(entity instanceof LivingEntity living))
            return null;
        ItemStack schedule = AllItems.SCHEDULE.getDefaultStack();
        if (player.getItemCooldownManager().isCoolingDown(schedule))
            return null;

        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.getItem() instanceof ScheduleItem si) {
            ActionResult result = si.handScheduleTo(itemStack, player, living, hand);
            if (result.isAccepted()) {
                player.getItemCooldownManager().set(schedule, 5);
                return result;
            }
        }

        if (hand == Hand.OFF_HAND)
            return null;

        Contraption contraption = cce.getContraption();
        if (!(contraption instanceof CarriageContraption cc))
            return null;

        Train train = cce.getCarriage().train;
        if (train == null)
            return null;
        if (train.runtime.getSchedule() == null)
            return null;

        Integer seatIndex = contraption.getSeatMapping().get(entity.getUuid());
        if (seatIndex == null)
            return null;
        BlockPos seatPos = contraption.getSeats().get(seatIndex);
        Couple<Boolean> directions = cc.conductorSeats.get(seatPos);
        if (directions == null)
            return null;

        World world = player.getWorld();
        boolean onServer = !world.isClient();

        if (train.runtime.paused && !train.runtime.completed) {
            if (onServer) {
                train.runtime.paused = false;
                AllSoundEvents.CONFIRM.playOnServer(world, player.getBlockPos(), 1, 1);
                player.sendMessage(Text.translatable("create.schedule.continued"), true);
            }

            player.getItemCooldownManager().set(schedule, 5);
            return ActionResult.SUCCESS;
        }

        if (!itemStack.isEmpty()) {
            if (onServer) {
                AllSoundEvents.DENY.playOnServer(world, player.getBlockPos(), 1, 1);
                player.sendMessage(Text.translatable("create.schedule.remove_with_empty_hand"), true);
            }
            return ActionResult.SUCCESS;
        }

        if (onServer) {
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .2f, 1f + world.random.nextFloat());
            player.sendMessage(
                Text.translatable(train.runtime.isAutoSchedule ? "create.schedule.auto_removed_from_train" : "create.schedule.removed_from_train"),
                true
            );

            player.getInventory().offerOrDrop(train.runtime.returnSchedule(player.getRegistryManager()));
        }

        player.getItemCooldownManager().set(schedule, 5);
        return ActionResult.SUCCESS;
    }
}