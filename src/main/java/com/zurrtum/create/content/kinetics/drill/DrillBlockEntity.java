package com.zurrtum.create.content.kinetics.drill;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.drill.CobbleGenOptimisation.CobbleGenBlockConfiguration;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;

public class DrillBlockEntity extends BlockBreakingKineticBlockEntity {

    private CobbleGenBlockConfiguration currentConfig;
    private BlockState currentOutput;

    public DrillBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.DRILL, pos, state);
        currentOutput = Blocks.AIR.getDefaultState();
    }

    @Override
    protected BlockPos getBreakingPos() {
        return getPos().offset(getCachedState().get(DrillBlock.FACING));
    }

    @Override
    public void onBlockBroken(BlockState stateToBreak) {
        if (!optimiseCobbleGen(stateToBreak))
            super.onBlockBroken(stateToBreak);
    }

    public boolean optimiseCobbleGen(BlockState stateToBreak) {
        DirectBeltInputBehaviour inv = BlockEntityBehaviour.get(world, breakingPos.down(), DirectBeltInputBehaviour.TYPE);
        BlockEntity blockEntityBelow = world.getBlockEntity(breakingPos.down());
        BlockEntity blockEntityAbove = world.getBlockEntity(breakingPos.up());

        if (inv == null && !(blockEntityBelow instanceof HopperBlockEntity) && !(blockEntityAbove instanceof ChuteBlockEntity chute && chute.getItemMotion() > 0))
            return false;

        CobbleGenBlockConfiguration config = CobbleGenOptimisation.getConfig(world, pos, getCachedState().get(DrillBlock.FACING));
        if (config == null)
            return false;
        if (!(world instanceof ServerWorld sl))
            return false;

        BlockPos breakingPos = getBreakingPos();
        if (!config.equals(currentConfig)) {
            currentConfig = config;
            currentOutput = CobbleGenOptimisation.determineOutput(sl, breakingPos, config);
        }

        if (currentOutput.isAir() || !currentOutput.equals(stateToBreak))
            return false;

        if (inv != null)
            for (ItemStack stack : Block.getDroppedStacks(stateToBreak, sl, breakingPos, null))
                inv.handleInsertion(stack, Direction.UP, false);
        else if (blockEntityBelow instanceof HopperBlockEntity hbe) {
            for (ItemStack stack : Block.getDroppedStacks(stateToBreak, sl, breakingPos, null))
                hbe.insertExist(stack);
        } else if (blockEntityAbove instanceof ChuteBlockEntity chute && chute.getItemMotion() > 0) {
            for (ItemStack stack : Block.getDroppedStacks(stateToBreak, sl, breakingPos, null))
                if (chute.getItem().isEmpty())
                    chute.setItem(stack, 0);
        }

        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, breakingPos, Block.getRawIdFromState(stateToBreak));
        return true;
    }

}