package com.zurrtum.create.content.trains.schedule;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.schedule.destination.DestinationInstruction;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class ScheduleItem extends Item implements MenuProvider, SupportsItemCopying {

    public ScheduleItem(Settings pProperties) {
        super(pProperties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() == null)
            return ActionResult.PASS;
        return use(context.getWorld(), context.getPlayer(), context.getHand());
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!player.isSneaking() && hand == Hand.MAIN_HAND) {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer)
                openHandledScreen(serverPlayer);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public ActionResult handScheduleTo(ItemStack pStack, PlayerEntity pPlayer, LivingEntity pInteractionTarget, Hand pUsedHand) {
        ActionResult pass = ActionResult.PASS;

        Schedule schedule = getSchedule(pPlayer.getRegistryManager(), pStack);
        if (schedule == null)
            return pass;
        if (pInteractionTarget == null)
            return pass;
        Entity rootVehicle = pInteractionTarget.getRootVehicle();
        if (!(rootVehicle instanceof CarriageContraptionEntity entity))
            return pass;
        if (pPlayer.getWorld().isClient)
            return ActionResult.SUCCESS;

        Contraption contraption = entity.getContraption();
        if (contraption instanceof CarriageContraption cc) {

            Train train = entity.getCarriage().train;
            if (train == null)
                return ActionResult.SUCCESS;

            Integer seatIndex = contraption.getSeatMapping().get(pInteractionTarget.getUuid());
            if (seatIndex == null)
                return ActionResult.SUCCESS;
            BlockPos seatPos = contraption.getSeats().get(seatIndex);
            Couple<Boolean> directions = cc.conductorSeats.get(seatPos);
            if (directions == null) {
                pPlayer.sendMessage(Text.translatable("create.schedule.non_controlling_seat"), true);
                AllSoundEvents.DENY.playOnServer(pPlayer.getWorld(), pPlayer.getBlockPos(), 1, 1);
                return ActionResult.SUCCESS;
            }

            if (train.runtime.getSchedule() != null) {
                AllSoundEvents.DENY.playOnServer(pPlayer.getWorld(), pPlayer.getBlockPos(), 1, 1);
                pPlayer.sendMessage(Text.translatable("create.schedule.remove_with_empty_hand"), true);
                return ActionResult.SUCCESS;
            }

            if (schedule.entries.isEmpty()) {
                AllSoundEvents.DENY.playOnServer(pPlayer.getWorld(), pPlayer.getBlockPos(), 1, 1);
                pPlayer.sendMessage(Text.translatable("create.schedule.no_stops"), true);
                return ActionResult.SUCCESS;
            }

            train.runtime.setSchedule(schedule, false);
            AllAdvancements.CONDUCTOR.trigger((ServerPlayerEntity) pPlayer);
            AllSoundEvents.CONFIRM.playOnServer(pPlayer.getWorld(), pPlayer.getBlockPos(), 1, 1);
            pPlayer.sendMessage(Text.translatable("create.schedule.applied_to_train").formatted(Formatting.GREEN), true);
            pStack.decrement(1);
            pPlayer.setStackInHand(pUsedHand, pStack.isEmpty() ? ItemStack.EMPTY : pStack);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplayComponent displayComponent,
        Consumer<Text> tooltip,
        TooltipType flagIn
    ) {
        Schedule schedule = getSchedule(context.getRegistryLookup(), stack);
        if (schedule == null || schedule.entries.isEmpty())
            return;

        MutableText caret = Text.literal("> ").formatted(Formatting.GRAY);
        MutableText arrow = Text.literal("-> ").formatted(Formatting.GRAY);

        List<ScheduleEntry> entries = schedule.entries;
        for (int i = 0; i < entries.size(); i++) {
            boolean current = i == schedule.savedProgress && schedule.entries.size() > 1;
            ScheduleEntry entry = entries.get(i);
            if (!(entry.instruction instanceof DestinationInstruction destination))
                continue;
            Formatting format = current ? Formatting.YELLOW : Formatting.GOLD;
            MutableText prefix = current ? arrow : caret;
            tooltip.accept(prefix.copy().append(Text.literal(destination.getFilter()).formatted(format)));
        }
    }

    public static Schedule getSchedule(RegistryWrapper.WrapperLookup registries, ItemStack pStack) {
        if (!pStack.contains(AllDataComponents.TRAIN_SCHEDULE))
            return null;
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleItem", Create.LOGGER)) {
            ReadView view = NbtReadView.create(logging, registries, pStack.get(AllDataComponents.TRAIN_SCHEDULE));
            return Schedule.read(view);
        }
    }

    @Override
    public ScheduleMenu createMenu(int id, PlayerInventory inv, PlayerEntity player, RegistryByteBuf extraData) {
        ItemStack heldItem = player.getMainHandStack();
        ItemStack.PACKET_CODEC.encode(extraData, heldItem);
        return new ScheduleMenu(id, inv, heldItem);
    }

    @Override
    public Text getDisplayName() {
        return getName();
    }

    @Override
    public ComponentType<?> getComponentType() {
        return AllDataComponents.TRAIN_SCHEDULE;
    }

}
