package com.zurrtum.create.content.kinetics.base;

//import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;

import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class KineticBlock extends Block implements IRotate {

    public KineticBlock(AbstractBlock.Settings properties) {
        super(properties);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        // onBlockAdded is useless for init, as sometimes the BE gets re-instantiated

        // however, if a block change occurs that does not change kinetic connections,
        // we can prevent a major re-propagation here

        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (blockEntity instanceof KineticBlockEntity kineticBlockEntity) {
            kineticBlockEntity.preventSpeedUpdate = 0;

            if (oldState.getBlock() != state.getBlock())
                return;
            if (state.hasBlockEntity() != oldState.hasBlockEntity())
                return;
            if (!areStatesKineticallyEquivalent(oldState, state))
                return;

            kineticBlockEntity.preventSpeedUpdate = 2;
        }
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        if (oldState.getBlock() != newState.getBlock())
            return false;
        return getRotationAxis(newState) == getRotationAxis(oldState);
    }

    @Override
    public void prepare(BlockState stateIn, WorldAccess worldIn, BlockPos pos, int flags, int count) {
        if (worldIn.isClient())
            return;

        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (!(blockEntity instanceof KineticBlockEntity kbe))
            return;

        if (kbe.preventSpeedUpdate > 0)
            return;

        // Remove previous information when block is added
        kbe.warnOfMovement();
        kbe.clearKineticInformation();
        kbe.updateSpeed = true;
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        AdvancementBehaviour.setPlacedBy(worldIn, pos, placer);
        if (worldIn.isClient)
            return;

        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (!(blockEntity instanceof KineticBlockEntity kbe))
            return;

        kbe.effects.queueRotationIndicators();
    }

    public float getParticleTargetRadius() {
        return .65f;
    }

    public float getParticleInitialRadius() {
        return .75f;
    }

}