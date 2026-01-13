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
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ValueSettingsInputHandler {
    public static ActionResult onBlockActivated(World world, ClientPlayerEntity player, Hand hand, BlockHitResult ray) {
        if (!canInteract(player))
            return null;
        ItemStack stack = player.getMainHandStack();
        if (stack.isOf(AllItems.CLIPBOARD))
            return null;
        BlockPos pos = ray.getBlockPos();
        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity sbe))
            return null;

        if (Create.VALUE_SETTINGS_HANDLER.cancelIfWarmupAlreadyStarted(pos)) {
            return ActionResult.FAIL;
        }

        if (sbe instanceof FactoryPanelBlockEntity fpbe) {
            for (FilteringBehaviour<?> behaviour : FactoryPanelBehaviour.allBehaviours(fpbe)) {
                ActionResult result = handleInteraction(behaviour, behaviour.getType(), player, hand, ray, stack, pos, sbe);
                if (result != null) {
                    return result;
                }
            }
        } else {
            ScrollValueBehaviour<?, ?> scrollValueBehaviour = sbe.getBehaviour(ScrollValueBehaviour.TYPE);
            if (scrollValueBehaviour != null) {
                ActionResult result = handleInteraction(scrollValueBehaviour, ScrollValueBehaviour.TYPE, player, hand, ray, stack, pos, sbe);
                if (result != null) {
                    return result;
                }
            }
            FilteringBehaviour<?> filteringBehaviour = sbe.getBehaviour(FilteringBehaviour.TYPE);
            if (filteringBehaviour instanceof SidedFilteringBehaviour sidedBehaviour) {
                filteringBehaviour = sidedBehaviour.get(ray.getSide());
            }
            if (filteringBehaviour != null) {
                return handleInteraction(filteringBehaviour, FilteringBehaviour.TYPE, player, hand, ray, stack, pos, sbe);
            }
        }
        return null;
    }

    private static ActionResult handleInteraction(
        BlockEntityBehaviour<?> behaviour,
        BehaviourType<? extends BlockEntityBehaviour<?>> type,
        ClientPlayerEntity player,
        Hand hand,
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
        if (valueSettingsBehaviour.onlyVisibleWithWrench() && !player.getStackInHand(hand).getRegistryEntry().isIn(AllItemTags.TOOLS_WRENCH))
            return null;
        if (valueSettingsBehaviour.getSlotPositioning() instanceof ValueBoxTransform.Sided sidedSlot) {
            if (!sidedSlot.isSideActive(sbe.getCachedState(), ray.getSide()))
                return null;
            sidedSlot.fromSide(ray.getSide());
        }

        boolean fakePlayer = FakePlayerHandler.has(player);
        if (!valueSettingsBehaviour.testHit(ray.getPos()))
            return null;

        if (!valueSettingsBehaviour.acceptsValueSettings() || fakePlayer) {
            valueSettingsBehaviour.onShortInteract(player, hand, ray.getSide(), ray);
            player.networkHandler.sendPacket(new ValueSettingsPacket(pos, 0, 0, hand, ray, ray.getSide(), false, valueSettingsBehaviour.netId()));
            return ActionResult.SUCCESS;
        }

        Create.VALUE_SETTINGS_HANDLER.startInteractionWith(pos, type, hand, ray.getSide());
        return ActionResult.SUCCESS;
    }

    public static boolean canInteract(PlayerEntity player) {
        return player != null && !player.isSpectator() && !player.isSneaking() && player.canModifyBlocks();
    }
}
