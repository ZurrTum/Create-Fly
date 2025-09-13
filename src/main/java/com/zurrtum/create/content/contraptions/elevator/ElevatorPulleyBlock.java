package com.zurrtum.create.content.contraptions.elevator;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class ElevatorPulleyBlock extends HorizontalKineticBlock implements IBE<ElevatorPulleyBlockEntity> {

    public ElevatorPulleyBlock(Settings properties) {
        super(properties);
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
        if (!player.canModifyBlocks())
            return ActionResult.FAIL;
        if (player.isSneaking())
            return ActionResult.FAIL;
        if (!stack.isEmpty())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient)
            return ActionResult.SUCCESS;
        return onBlockEntityUseItemOn(
            level, pos, be -> {
                be.clicked();
                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public BlockEntityType<? extends ElevatorPulleyBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ELEVATOR_PULLEY;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(HORIZONTAL_FACING).rotateYClockwise().getAxis();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.ELEVATOR_PULLEY.get(state.get(HORIZONTAL_FACING));
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return getRotationAxis(state) == face.getAxis();
    }

    @Override
    public Class<ElevatorPulleyBlockEntity> getBlockEntityClass() {
        return ElevatorPulleyBlockEntity.class;
    }

}
