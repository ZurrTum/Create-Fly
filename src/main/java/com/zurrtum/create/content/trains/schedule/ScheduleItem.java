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
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

public class ScheduleItem extends Item implements MenuProvider, SupportsItemCopying {

    public ScheduleItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null)
            return InteractionResult.PASS;
        return use(context.getLevel(), context.getPlayer(), context.getHand());
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer)
                openHandledScreen(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public InteractionResult handScheduleTo(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        InteractionResult pass = InteractionResult.PASS;

        Schedule schedule = getSchedule(pPlayer.registryAccess(), pStack);
        if (schedule == null)
            return pass;
        if (pInteractionTarget == null)
            return pass;
        Entity rootVehicle = pInteractionTarget.getRootVehicle();
        if (!(rootVehicle instanceof CarriageContraptionEntity entity))
            return pass;
        if (pPlayer.level().isClientSide())
            return InteractionResult.SUCCESS;

        Contraption contraption = entity.getContraption();
        if (contraption instanceof CarriageContraption cc) {

            Train train = entity.getCarriage().train;
            if (train == null)
                return InteractionResult.SUCCESS;

            Integer seatIndex = contraption.getSeatMapping().get(pInteractionTarget.getUUID());
            if (seatIndex == null)
                return InteractionResult.SUCCESS;
            BlockPos seatPos = contraption.getSeats().get(seatIndex);
            Couple<Boolean> directions = cc.conductorSeats.get(seatPos);
            if (directions == null) {
                pPlayer.displayClientMessage(Component.translatable("create.schedule.non_controlling_seat"), true);
                AllSoundEvents.DENY.playOnServer(pPlayer.level(), pPlayer.blockPosition(), 1, 1);
                return InteractionResult.SUCCESS;
            }

            if (train.runtime.getSchedule() != null) {
                AllSoundEvents.DENY.playOnServer(pPlayer.level(), pPlayer.blockPosition(), 1, 1);
                pPlayer.displayClientMessage(Component.translatable("create.schedule.remove_with_empty_hand"), true);
                return InteractionResult.SUCCESS;
            }

            if (schedule.entries.isEmpty()) {
                AllSoundEvents.DENY.playOnServer(pPlayer.level(), pPlayer.blockPosition(), 1, 1);
                pPlayer.displayClientMessage(Component.translatable("create.schedule.no_stops"), true);
                return InteractionResult.SUCCESS;
            }

            train.runtime.setSchedule(schedule, false);
            AllAdvancements.CONDUCTOR.trigger((ServerPlayer) pPlayer);
            AllSoundEvents.CONFIRM.playOnServer(pPlayer.level(), pPlayer.blockPosition(), 1, 1);
            pPlayer.displayClientMessage(Component.translatable("create.schedule.applied_to_train").withStyle(ChatFormatting.GREEN), true);
            pStack.shrink(1);
            pPlayer.setItemInHand(pUsedHand, pStack.isEmpty() ? ItemStack.EMPTY : pStack);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplay displayComponent,
        Consumer<Component> tooltip,
        TooltipFlag flagIn
    ) {
        Schedule schedule = getSchedule(context.registries(), stack);
        if (schedule == null || schedule.entries.isEmpty())
            return;

        MutableComponent caret = Component.literal("> ").withStyle(ChatFormatting.GRAY);
        MutableComponent arrow = Component.literal("-> ").withStyle(ChatFormatting.GRAY);

        List<ScheduleEntry> entries = schedule.entries;
        for (int i = 0; i < entries.size(); i++) {
            boolean current = i == schedule.savedProgress && schedule.entries.size() > 1;
            ScheduleEntry entry = entries.get(i);
            if (!(entry.instruction instanceof DestinationInstruction destination))
                continue;
            ChatFormatting format = current ? ChatFormatting.YELLOW : ChatFormatting.GOLD;
            MutableComponent prefix = current ? arrow : caret;
            tooltip.accept(prefix.copy().append(Component.literal(destination.getFilter()).withStyle(format)));
        }
    }

    public static Schedule getSchedule(HolderLookup.Provider registries, ItemStack pStack) {
        if (!pStack.has(AllDataComponents.TRAIN_SCHEDULE))
            return null;
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleItem", Create.LOGGER)) {
            ValueInput view = TagValueInput.create(logging, registries, pStack.get(AllDataComponents.TRAIN_SCHEDULE));
            return Schedule.read(view);
        }
    }

    @Override
    public ScheduleMenu createMenu(int id, Inventory inv, Player player, RegistryFriendlyByteBuf extraData) {
        ItemStack heldItem = player.getMainHandItem();
        ItemStack.STREAM_CODEC.encode(extraData, heldItem);
        return new ScheduleMenu(id, inv, heldItem);
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public DataComponentType<?> getComponentType() {
        return AllDataComponents.TRAIN_SCHEDULE;
    }

}
