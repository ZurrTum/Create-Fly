package com.zurrtum.create.content.equipment;

import com.zurrtum.create.catnip.levelWrappers.PlacementSimulationServerLevel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TreeFertilizerItem extends Item {

    public TreeFertilizerItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockState state = world.getBlockState(context.getBlockPos());
        Block block = state.getBlock();
        if (block instanceof Fertilizable bonemealableBlock && state.isIn(BlockTags.SAPLINGS)) {

            if (state.get(Properties.HANGING, false))
                return ActionResult.PASS;

            if (world.isClient()) {
                BoneMealItem.createParticles(world, context.getBlockPos(), 100);
                return ActionResult.SUCCESS;
            }

            BlockPos saplingPos = context.getBlockPos();
            TreesDreamWorld treesDreamWorld = new TreesDreamWorld((ServerWorld) world, saplingPos);

            for (BlockPos pos : BlockPos.iterate(-1, 0, -1, 1, 0, 1)) {
                if (world.getBlockState(saplingPos.add(pos)).getBlock() == block)
                    treesDreamWorld.setBlockState(pos.up(10), withStage(state, 1));
            }

            bonemealableBlock.grow(treesDreamWorld, treesDreamWorld.getRandom(), BlockPos.ORIGIN.up(10), withStage(state, 1));

            for (BlockPos pos : treesDreamWorld.blocksAdded.keySet()) {
                BlockPos actualPos = pos.add(saplingPos).down(10);
                BlockState newState = treesDreamWorld.blocksAdded.get(pos);

                // Don't replace Bedrock
                if (world.getBlockState(actualPos).getHardness(world, actualPos) == -1)
                    continue;
                // Don't replace solid blocks with leaves
                if (!newState.isSolidBlock(treesDreamWorld, pos) && !world.getBlockState(actualPos).getCollisionShape(world, actualPos).isEmpty())
                    continue;

                world.breakBlock(actualPos, true);
                world.setBlockState(actualPos, newState);
            }

            if (context.getPlayer() != null && !context.getPlayer().isCreative())
                context.getStack().decrement(1);
            return ActionResult.SUCCESS;

        }

        return super.useOnBlock(context);
    }

    private BlockState withStage(BlockState original, int stage) {
        if (!original.contains(Properties.STAGE))
            return original;
        return original.with(Properties.STAGE, stage);
    }

    private static class TreesDreamWorld extends PlacementSimulationServerLevel {
        private final BlockState soil;

        protected TreesDreamWorld(ServerWorld wrapped, BlockPos saplingPos) {
            super(wrapped);
            BlockState stateUnderSapling = wrapped.getBlockState(saplingPos.down());

            // Tree features don't seem to succeed with mud as soil
            if (stateUnderSapling.isIn(BlockTags.DIRT))
                stateUnderSapling = Blocks.DIRT.getDefaultState();

            soil = stateUnderSapling;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            if (pos.getY() <= 9)
                return soil;
            return super.getBlockState(pos);
        }

        @Override
        public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
            if (newState.getBlock() == Blocks.PODZOL)
                return true;
            return super.setBlockState(pos, newState, flags);
        }
    }

}
