package com.zurrtum.create.content.redstone.link.controller;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class LinkedControllerItem extends Item implements MenuProvider {

    public LinkedControllerItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static InteractionResult onItemUseFirst(
        Level world,
        Player player,
        ItemStack stack,
        InteractionHand hand,
        BlockHitResult ray,
        BlockPos pos
    ) {
        if (stack.getItem() instanceof LinkedControllerItem item) {
            if (player.mayBuild()) {
                BlockState hitState = world.getBlockState(pos);
                if (player.isShiftKeyDown()) {
                    if (hitState.is(AllBlocks.LECTERN_CONTROLLER)) {
                        if (!world.isClientSide())
                            AllBlocks.LECTERN_CONTROLLER.withBlockEntityDo(world, pos, be -> be.swapControllers(stack, player, hand, hitState));
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    if (hitState.is(AllBlocks.REDSTONE_LINK)) {
                        if (world.isClientSide())
                            AllClientHandle.INSTANCE.toggleLinkedControllerBindMode(pos);
                        player.getCooldowns().addCooldown(stack, 2);
                        return InteractionResult.CONSUME;
                    }

                    if (hitState.is(Blocks.LECTERN) && !hitState.getValue(LecternBlock.HAS_BOOK)) {
                        if (!world.isClientSide()) {
                            ItemStack lecternStack = player.isCreative() ? stack.copy() : stack.split(1);
                            AllBlocks.LECTERN_CONTROLLER.replaceLectern(hitState, world, pos, lecternStack);
                        }
                        return InteractionResult.SUCCESS;
                    }

                    if (hitState.is(AllBlocks.LECTERN_CONTROLLER))
                        return InteractionResult.PASS;
                }
            }

            return item.use(world, player, hand);
        }
        return null;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer && player.mayBuild())
                openHandledScreen(serverPlayer);
            return InteractionResult.SUCCESS;
        }

        if (!player.isShiftKeyDown()) {
            if (world.isClientSide())
                AllClientHandle.INSTANCE.toggleLinkedControllerActive();
            player.getCooldowns().addCooldown(heldItem, 2);
        }

        return InteractionResult.PASS;
    }

    public static ItemStackHandler getFrequencyItems(ItemStack stack) {
        ItemStackHandler newInv = new ItemStackHandler(12);
        if (!stack.is(AllItems.LINKED_CONTROLLER))
            throw new IllegalArgumentException("Cannot get frequency items from non-controller: " + stack);
        if (!stack.has(AllDataComponents.LINKED_CONTROLLER_ITEMS))
            return newInv;
        ItemHelper.fillItemStackHandler(stack.getOrDefault(AllDataComponents.LINKED_CONTROLLER_ITEMS, ItemContainerContents.EMPTY), newInv);
        return newInv;
    }

    public static Couple<Frequency> toFrequency(ItemStack controller, int slot) {
        ItemStackHandler frequencyItems = getFrequencyItems(controller);
        return Couple.create(Frequency.of(frequencyItems.getItem(slot * 2)), Frequency.of(frequencyItems.getItem(slot * 2 + 1)));
    }

    @Override
    public @Nullable MenuBase<?> createMenu(int id, Inventory inv, Player player, RegistryFriendlyByteBuf extraData) {
        ItemStack heldItem = player.getMainHandItem();
        ItemStack.STREAM_CODEC.encode(extraData, heldItem);
        return new LinkedControllerMenu(id, inv, heldItem);
    }

    @Override
    public Component getDisplayName() {
        return components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
    }
}
