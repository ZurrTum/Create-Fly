package com.zurrtum.create.content.redstone;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

public class DirectedDirectionalBlock extends HorizontalDirectionalBlock implements IWrenchable, TransformableBlock {

    public static final EnumProperty<AttachFace> TARGET = EnumProperty.create("target", AttachFace.class);

    public static final MapCodec<DirectedDirectionalBlock> CODEC = simpleCodec(DirectedDirectionalBlock::new);

    public DirectedDirectionalBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(TARGET, AttachFace.WALL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(TARGET, FACING));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        for (Direction direction : pContext.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Axis.Y) {
                blockstate = defaultBlockState().setValue(TARGET, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)
                    .setValue(FACING, pContext.getHorizontalDirection());
            } else {
                blockstate = defaultBlockState().setValue(TARGET, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            return blockstate;
        }

        return null;
    }

    public static Direction getTargetDirection(BlockState pState) {
        return switch (pState.getValue(TARGET)) {
            case CEILING -> Direction.UP;
            case FLOOR -> Direction.DOWN;
            default -> pState.getValue(FACING);
        };
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == Axis.Y)
            return IWrenchable.super.getRotatedBlockState(originalState, targetedFace);

        Direction targetDirection = getTargetDirection(originalState);
        Direction newFacing = targetDirection.getClockWise(targetedFace.getAxis());
        if (targetedFace.getAxisDirection() == AxisDirection.NEGATIVE)
            newFacing = newFacing.getOpposite();

        if (newFacing.getAxis() == Axis.Y)
            return originalState.setValue(TARGET, newFacing == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR);
        return originalState.setValue(TARGET, AttachFace.WALL).setValue(FACING, newFacing);
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        if (transform.mirror != null)
            state = mirror(state, transform.mirror);
        if (transform.rotationAxis == Axis.Y)
            return rotate(state, transform.rotation);

        Direction targetDirection = getTargetDirection(state);
        Direction newFacing = transform.rotateFacing(targetDirection);

        if (newFacing.getAxis() == Axis.Y)
            return state.setValue(TARGET, newFacing == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR);
        return state.setValue(TARGET, AttachFace.WALL).setValue(FACING, newFacing);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
