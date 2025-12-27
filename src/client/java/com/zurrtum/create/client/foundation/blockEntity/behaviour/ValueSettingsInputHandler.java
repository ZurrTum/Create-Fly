package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.packet.c2s.ValueSettingsPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class ValueSettingsInputHandler {
    public static InteractionResult onBlockActivated(Level world, LocalPlayer player, InteractionHand hand, BlockHitResult ray) {
        if (!canInteract(player))
            return null;
        ItemStack stack = player.getMainHandItem();
        if (stack.is(AllItems.CLIPBOARD))
            return null;
        BlockPos pos = ray.getBlockPos();
        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity sbe))
            return null;

        if (Create.VALUE_SETTINGS_HANDLER.cancelIfWarmupAlreadyStarted(pos)) {
            return InteractionResult.FAIL;
        }

        if (sbe instanceof FactoryPanelBlockEntity fpbe) {
            for (FilteringBehaviour<?> behaviour : FactoryPanelBehaviour.allBehaviours(fpbe)) {
                InteractionResult result = handleInteraction(behaviour, behaviour.getType(), player, hand, ray, stack, pos, sbe);
                if (result != null) {
                    return result;
                }
            }
        } else {
            ScrollValueBehaviour<?, ?> scrollValueBehaviour = sbe.getBehaviour(ScrollValueBehaviour.TYPE);
            if (scrollValueBehaviour != null) {
                InteractionResult result = handleInteraction(scrollValueBehaviour, ScrollValueBehaviour.TYPE, player, hand, ray, stack, pos, sbe);
                if (result != null) {
                    return result;
                }
            }
            FilteringBehaviour<?> filteringBehaviour = sbe.getBehaviour(FilteringBehaviour.TYPE);
            if (filteringBehaviour instanceof SidedFilteringBehaviour sidedBehaviour) {
                filteringBehaviour = sidedBehaviour.get(ray.getDirection());
            }
            if (filteringBehaviour != null) {
                return handleInteraction(filteringBehaviour, FilteringBehaviour.TYPE, player, hand, ray, stack, pos, sbe);
            }
        }
        return null;
    }

    private static InteractionResult handleInteraction(
        BlockEntityBehaviour<?> behaviour,
        BehaviourType<? extends BlockEntityBehaviour<?>> type,
        LocalPlayer player,
        InteractionHand hand,
        BlockHitResult ray,
        ItemStack stack,
        BlockPos pos,
        SmartBlockEntity sbe
    ) {
        ValueSettingsBehaviour valueSettingsBehaviour = (ValueSettingsBehaviour) behaviour;
        if (valueSettingsBehaviour.bypassesInput(stack))
            return null;
        if (!valueSettingsBehaviour.mayInteract(player))
            return null;

        if (!valueSettingsBehaviour.isActive())
            return null;
        if (valueSettingsBehaviour.onlyVisibleWithWrench() && !player.getItemInHand(hand).is(AllItemTags.TOOLS_WRENCH))
            return null;
        if (valueSettingsBehaviour.getSlotPositioning() instanceof ValueBoxTransform.Sided sidedSlot) {
            if (!sidedSlot.isSideActive(sbe.getBlockState(), ray.getDirection()))
                return null;
            sidedSlot.fromSide(ray.getDirection());
        }

        boolean fakePlayer = FakePlayerHandler.has(player);
        if (!valueSettingsBehaviour.testHit(ray.getLocation()))
            return null;

        if (!valueSettingsBehaviour.acceptsValueSettings() || fakePlayer) {
            valueSettingsBehaviour.onShortInteract(player, hand, ray.getDirection(), ray);
            player.connection.send(new ValueSettingsPacket(pos, 0, 0, hand, ray, ray.getDirection(), false, valueSettingsBehaviour.netId()));
            return InteractionResult.SUCCESS;
        }

        Create.VALUE_SETTINGS_HANDLER.startInteractionWith(pos, type, hand, ray.getDirection());
        return InteractionResult.SUCCESS;
    }

    public static boolean canInteract(Player player) {
        return player != null && !player.isSpectator() && !player.isShiftKeyDown() && player.mayBuild();
    }
}
