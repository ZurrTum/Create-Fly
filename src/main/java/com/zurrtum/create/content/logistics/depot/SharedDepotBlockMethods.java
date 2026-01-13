package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SharedDepotBlockMethods {

    protected static DepotBehaviour get(BlockView worldIn, BlockPos pos) {
        return BlockEntityBehaviour.get(worldIn, pos, DepotBehaviour.TYPE);
    }

    public static ActionResult onUse(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult ray
    ) {
        if (ray.getSide() != Direction.UP)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient)
            return ActionResult.SUCCESS;

        DepotBehaviour behaviour = get(level, pos);
        if (behaviour == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (!behaviour.canAcceptItems.get())
            return ActionResult.SUCCESS;

        boolean wasEmptyHanded = stack.isEmpty();
        boolean shouldntPlaceItem = stack.isOf(AllItems.MECHANICAL_ARM);

        ItemStack mainItemStack = behaviour.getHeldItemStack();
        if (!mainItemStack.isEmpty()) {
            if (ItemStack.areItemsAndComponentsEqual(mainItemStack, stack)) {
                int remainder = mainItemStack.getCount();
                int count = stack.getCount();
                int extract = Math.min(remainder, stack.getMaxCount() - count);
                if (extract != 0) {
                    stack.setCount(count + extract);
                    if (extract == remainder) {
                        mainItemStack = ItemStack.EMPTY;
                        behaviour.removeHeldItem();
                    } else {
                        mainItemStack.setCount(remainder - extract);
                    }
                }
            }
            if (!mainItemStack.isEmpty()) {
                player.getInventory().offerOrDrop(mainItemStack);
                behaviour.removeHeldItem();
                level.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .2f, 1f + level.getRandom().nextFloat());
            }
        }
        boolean change = false;
        Inventory outputs = behaviour.processingOutputBuffer;
        for (int i = 0, size = outputs.size(); i < size; i++) {
            ItemStack itemStack = outputs.removeStack(i);
            if (itemStack.isEmpty()) {
                continue;
            }
            player.getInventory().offerOrDrop(itemStack);
            change = true;
        }
        if (change) {
            outputs.markDirty();
        }

        if (!wasEmptyHanded && !shouldntPlaceItem) {
            TransportedItemStack transported = new TransportedItemStack(stack);
            transported.insertedFrom = player.getHorizontalFacing();
            transported.prevBeltPosition = .25f;
            transported.beltPosition = .25f;
            behaviour.setHeldItem(transported);
            player.setStackInHand(hand, ItemStack.EMPTY);
            AllSoundEvents.DEPOT_SLIDE.playOnServer(level, pos);
        }

        behaviour.blockEntity.notifyUpdate();
        return ActionResult.SUCCESS;
    }

    public static void onLanded(BlockView worldIn, Entity entityIn) {
        ItemStack asItem = ItemHelper.fromItemEntity(entityIn);
        if (asItem.isEmpty())
            return;
        if (entityIn.getWorld().isClient)
            return;

        BlockPos pos = entityIn.getBlockPos();
        DirectBeltInputBehaviour inputBehaviour = BlockEntityBehaviour.get(worldIn, pos, DirectBeltInputBehaviour.TYPE);
        if (inputBehaviour == null)
            return;
        Vec3d targetLocation = VecHelper.getCenterOf(pos).add(0, 5 / 16f, 0);
        if (!PackageEntity.centerPackage(entityIn, targetLocation))
            return;

        ItemStack remainder = inputBehaviour.handleInsertion(asItem, Direction.DOWN, false);
        if (entityIn instanceof ItemEntity)
            ((ItemEntity) entityIn).setStack(remainder);
        if (remainder.isEmpty())
            entityIn.discard();
    }

    public static int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        DepotBehaviour depotBehaviour = get(worldIn, pos);
        if (depotBehaviour == null)
            return 0;
        float f = depotBehaviour.getPresentStackSize();
        Integer max = depotBehaviour.maxStackSize.get();
        f = f / (max == 0 ? 64 : max);
        return MathHelper.clamp(MathHelper.floor(f * 14.0F) + (f > 0 ? 1 : 0), 0, 15);
    }

}
