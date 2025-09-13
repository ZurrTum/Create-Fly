package com.zurrtum.create.content.kinetics.steamEngine;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.AbstractShaftBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class PoweredShaftBlock extends AbstractShaftBlock {

    public PoweredShaftBlock(Settings properties) {
        super(properties);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.EIGHT_VOXEL_POLE.get(pState.get(AXIS));
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.POWERED_SHAFT;
    }

    @Override
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        if (player.isSneaking() || !player.canModifyBlocks())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        IPlacementHelper helper = PlacementHelpers.get(ShaftBlock.placementHelperId);
        if (helper.matchesItem(stack))
            return helper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        if (!stillValid(pState, pLevel, pPos))
            pLevel.setBlockState(
                pPos,
                AllBlocks.SHAFT.getDefaultState().with(ShaftBlock.AXIS, pState.get(AXIS)).with(WATERLOGGED, pState.get(WATERLOGGED)),
                Block.NOTIFY_ALL
            );
    }

    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.SHAFT.getDefaultStack();
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        return stillValid(pState, pLevel, pPos);
    }

    public static boolean stillValid(BlockState pState, WorldView pLevel, BlockPos pPos) {
        for (Direction d : Iterate.directions) {
            if (d.getAxis() == pState.get(AXIS))
                continue;
            BlockPos enginePos = pPos.offset(d, 2);
            BlockState engineState = pLevel.getBlockState(enginePos);
            if (!(engineState.getBlock() instanceof SteamEngineBlock engine))
                continue;
            if (!SteamEngineBlock.getShaftPos(engineState, enginePos).equals(pPos))
                continue;
            if (SteamEngineBlock.isShaftValid(engineState, pState))
                return true;
        }
        return false;
    }

    public static BlockState getEquivalent(BlockState stateForPlacement) {
        return AllBlocks.POWERED_SHAFT.getDefaultState().with(PoweredShaftBlock.AXIS, stateForPlacement.get(ShaftBlock.AXIS))
            .with(WATERLOGGED, stateForPlacement.get(WATERLOGGED));
    }

}
