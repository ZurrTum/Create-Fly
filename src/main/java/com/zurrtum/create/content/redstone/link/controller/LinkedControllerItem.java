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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LinkedControllerItem extends Item implements MenuProvider {

    public LinkedControllerItem(Settings properties) {
        super(properties);
    }

    public static ActionResult onItemUseFirst(World world, PlayerEntity player, ItemStack stack, Hand hand, BlockHitResult ray, BlockPos pos) {
        if (stack.getItem() instanceof LinkedControllerItem item) {
            if (player.canModifyBlocks()) {
                BlockState hitState = world.getBlockState(pos);
                if (player.isSneaking()) {
                    if (hitState.isOf(AllBlocks.LECTERN_CONTROLLER)) {
                        if (!world.isClient)
                            AllBlocks.LECTERN_CONTROLLER.withBlockEntityDo(world, pos, be -> be.swapControllers(stack, player, hand, hitState));
                        return ActionResult.SUCCESS;
                    }
                } else {
                    if (hitState.isOf(AllBlocks.REDSTONE_LINK)) {
                        if (world.isClient)
                            AllClientHandle.INSTANCE.toggleLinkedControllerBindMode(pos);
                        player.getItemCooldownManager().set(stack, 2);
                        return ActionResult.CONSUME;
                    }

                    if (hitState.isOf(Blocks.LECTERN) && !hitState.get(LecternBlock.HAS_BOOK)) {
                        if (!world.isClient) {
                            ItemStack lecternStack = player.isCreative() ? stack.copy() : stack.split(1);
                            AllBlocks.LECTERN_CONTROLLER.replaceLectern(hitState, world, pos, lecternStack);
                        }
                        return ActionResult.SUCCESS;
                    }

                    if (hitState.isOf(AllBlocks.LECTERN_CONTROLLER))
                        return ActionResult.PASS;
                }
            }

            return item.use(world, player, hand);
        }
        return null;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack heldItem = player.getStackInHand(hand);

        if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer && player.canModifyBlocks())
                openHandledScreen(serverPlayer);
            return ActionResult.SUCCESS;
        }

        if (!player.isSneaking()) {
            if (world.isClient)
                AllClientHandle.INSTANCE.toggleLinkedControllerActive();
            player.getItemCooldownManager().set(heldItem, 2);
        }

        return ActionResult.PASS;
    }

    public static ItemStackHandler getFrequencyItems(ItemStack stack) {
        ItemStackHandler newInv = new ItemStackHandler(12);
        if (!stack.isOf(AllItems.LINKED_CONTROLLER))
            throw new IllegalArgumentException("Cannot get frequency items from non-controller: " + stack);
        if (!stack.contains(AllDataComponents.LINKED_CONTROLLER_ITEMS))
            return newInv;
        ItemHelper.fillItemStackHandler(stack.getOrDefault(AllDataComponents.LINKED_CONTROLLER_ITEMS, ContainerComponent.DEFAULT), newInv);
        return newInv;
    }

    public static Couple<Frequency> toFrequency(ItemStack controller, int slot) {
        ItemStackHandler frequencyItems = getFrequencyItems(controller);
        return Couple.create(Frequency.of(frequencyItems.getStack(slot * 2)), Frequency.of(frequencyItems.getStack(slot * 2 + 1)));
    }

    @Override
    public @Nullable MenuBase<?> createMenu(int id, PlayerInventory inv, PlayerEntity player, RegistryByteBuf extraData) {
        ItemStack heldItem = player.getMainHandStack();
        ItemStack.PACKET_CODEC.encode(extraData, heldItem);
        return new LinkedControllerMenu(id, inv, heldItem);
    }

    @Override
    public Text getDisplayName() {
        return getName();
    }
}
