package com.zurrtum.create.content.equipment;

import com.zurrtum.create.catnip.levelWrappers.PlacementSimulationServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TreeFertilizerItem extends Item {

    public TreeFertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockState state = world.getBlockState(context.getClickedPos());
        Block block = state.getBlock();
        if (block instanceof BonemealableBlock bonemealableBlock && state.is(BlockTags.SAPLINGS)) {

            if (state.getValueOrElse(BlockStateProperties.HANGING, false))
                return InteractionResult.PASS;

            if (world.isClientSide()) {
                BoneMealItem.addGrowthParticles(world, context.getClickedPos(), 100);
                return InteractionResult.SUCCESS;
            }

            BlockPos saplingPos = context.getClickedPos();
            TreesDreamWorld treesDreamWorld = new TreesDreamWorld((ServerLevel) world, saplingPos);

            for (BlockPos pos : BlockPos.betweenClosed(-1, 0, -1, 1, 0, 1)) {
                if (world.getBlockState(saplingPos.offset(pos)).getBlock() == block)
                    treesDreamWorld.setBlockAndUpdate(pos.above(10), withStage(state, 1));
            }

            bonemealableBlock.performBonemeal(treesDreamWorld, treesDreamWorld.getRandom(), BlockPos.ZERO.above(10), withStage(state, 1));

            for (BlockPos pos : treesDreamWorld.blocksAdded.keySet()) {
                BlockPos actualPos = pos.offset(saplingPos).below(10);
                BlockState newState = treesDreamWorld.blocksAdded.get(pos);

                // Don't replace Bedrock
                if (world.getBlockState(actualPos).getDestroySpeed(world, actualPos) == -1)
                    continue;
                // Don't replace solid blocks with leaves
                if (!newState.isRedstoneConductor(treesDreamWorld, pos) && !world.getBlockState(actualPos).getCollisionShape(world, actualPos).isEmpty())
                    continue;

                world.setBlockAndUpdate(actualPos, newState);
            }

            if (context.getPlayer() != null && !context.getPlayer().isCreative())
                context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;

        }

        return super.useOn(context);
    }

    private BlockState withStage(BlockState original, int stage) {
        if (!original.hasProperty(BlockStateProperties.STAGE))
            return original;
        return original.setValue(BlockStateProperties.STAGE, stage);
    }

    private static class TreesDreamWorld extends PlacementSimulationServerLevel {
        private final BlockState soil;

        protected TreesDreamWorld(ServerLevel wrapped, BlockPos saplingPos) {
            super(wrapped);
            BlockState stateUnderSapling = wrapped.getBlockState(saplingPos.below());

            // Tree features don't seem to succeed with mud as soil
            if (stateUnderSapling.is(BlockTags.DIRT))
                stateUnderSapling = Blocks.DIRT.defaultBlockState();

            soil = stateUnderSapling;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            if (pos.getY() <= 9)
                return soil;
            return super.getBlockState(pos);
        }

        @Override
        public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
            if (newState.getBlock() == Blocks.PODZOL)
                return true;
            return super.setBlock(pos, newState, flags);
        }
    }

}
