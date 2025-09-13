package com.zurrtum.create.content.kinetics.mixer;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.KineticBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class MechanicalMixerBlock extends KineticBlock implements IBE<MechanicalMixerBlockEntity>, ICogWheel {

    public MechanicalMixerBlock(Settings properties) {
        super(properties);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        return !BasinBlock.isBasin(worldIn, pos.down());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext entityShapeContext && entityShapeContext.getEntity() instanceof PlayerEntity)
            return AllShapes.CASING_14PX.get(Direction.DOWN);

        return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public float getParticleTargetRadius() {
        return .85f;
    }

    @Override
    public float getParticleInitialRadius() {
        return .75f;
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.MEDIUM;
    }

    @Override
    public Class<MechanicalMixerBlockEntity> getBlockEntityClass() {
        return MechanicalMixerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalMixerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.MECHANICAL_MIXER;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

}
