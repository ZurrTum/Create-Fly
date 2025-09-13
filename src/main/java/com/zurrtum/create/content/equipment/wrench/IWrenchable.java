package com.zurrtum.create.content.equipment.wrench;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.content.kinetics.base.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface IWrenchable {

    static void playRemoveSound(World level, BlockPos pos) {
        AllSoundEvents.WRENCH_REMOVE.playOnServer(level, pos, 1, level.random.nextFloat() * .5f + .5f);
    }

    static void playRotateSound(World level, BlockPos pos) {
        AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos, 1, level.random.nextFloat() + .5f);
    }

    default ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState rotated = getRotatedBlockState(state, context.getSide());
        if (!rotated.canPlaceAt(level, context.getBlockPos()))
            return ActionResult.PASS;

        KineticBlockEntity.switchToBlockState(level, pos, updateAfterWrenched(rotated, context));

        if (level.getBlockState(pos) != state)
            playRotateSound(level, pos);

        return ActionResult.SUCCESS;
    }

    default BlockState updateAfterWrenched(BlockState newState, ItemUsageContext context) {
        //		return newState;
        return Block.postProcessState(newState, context.getWorld(), context.getBlockPos());
    }

    default ActionResult onSneakWrenched(BlockState state, ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        if (!(world instanceof ServerWorld serverLevel))
            return ActionResult.SUCCESS;

        BlockEntity be = world.getBlockEntity(pos);
        //TODO
        //        boolean shouldBreak = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, player, pos, state, be);
        //        if (!shouldBreak) {
        //            PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(world, player, pos, state, be);
        //            return ActionResult.SUCCESS;
        //        }

        if (player != null && !player.isCreative()) {
            Block.getDroppedStacks(state, serverLevel, pos, world.getBlockEntity(pos), player, context.getStack()).forEach(itemStack -> {
                player.getInventory().offerOrDrop(itemStack);
            });
        }

        state.onStacksDropped(serverLevel, pos, ItemStack.EMPTY, true);
        world.breakBlock(pos, false);
        playRemoveSound(world, pos);
        //        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(world, player, pos, state, be);
        return ActionResult.SUCCESS;
    }

    default BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        BlockState newState = originalState;

        if (targetedFace.getAxis() == Direction.Axis.Y) {
            if (originalState.contains(HorizontalAxisKineticBlock.HORIZONTAL_AXIS))
                return originalState.with(
                    HorizontalAxisKineticBlock.HORIZONTAL_AXIS,
                    VoxelShaper.axisAsFace(originalState.get(HorizontalAxisKineticBlock.HORIZONTAL_AXIS)).rotateClockwise(targetedFace.getAxis())
                        .getAxis()
                );
            if (originalState.contains(HorizontalKineticBlock.HORIZONTAL_FACING))
                return originalState.with(
                    HorizontalKineticBlock.HORIZONTAL_FACING,
                    originalState.get(HorizontalKineticBlock.HORIZONTAL_FACING).rotateClockwise(targetedFace.getAxis())
                );
        }

        if (originalState.contains(RotatedPillarKineticBlock.AXIS))
            return originalState.with(
                RotatedPillarKineticBlock.AXIS,
                VoxelShaper.axisAsFace(originalState.get(RotatedPillarKineticBlock.AXIS)).rotateClockwise(targetedFace.getAxis()).getAxis()
            );

        if (!originalState.contains(DirectionalKineticBlock.FACING))
            return originalState;

        Direction stateFacing = originalState.get(DirectionalKineticBlock.FACING);

        if (stateFacing.getAxis().equals(targetedFace.getAxis())) {
            if (originalState.contains(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE))
                return originalState.cycle(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
            else
                return originalState;
        } else {
            do {
                newState = newState.with(
                    DirectionalKineticBlock.FACING,
                    newState.get(DirectionalKineticBlock.FACING).rotateClockwise(targetedFace.getAxis())
                );
                if (targetedFace.getAxis() == Direction.Axis.Y && newState.contains(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE))
                    newState = newState.cycle(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
            } while (newState.get(DirectionalKineticBlock.FACING).getAxis().equals(targetedFace.getAxis()));
        }
        return newState;
    }
}
