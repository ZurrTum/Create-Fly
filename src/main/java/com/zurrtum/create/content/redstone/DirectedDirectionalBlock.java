package com.zurrtum.create.content.redstone;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DirectedDirectionalBlock extends HorizontalFacingBlock implements IWrenchable, TransformableBlock {

    public static final EnumProperty<BlockFace> TARGET = EnumProperty.of("target", BlockFace.class);

    public static final MapCodec<DirectedDirectionalBlock> CODEC = createCodec(DirectedDirectionalBlock::new);

    public DirectedDirectionalBlock(Settings pProperties) {
        super(pProperties);
        setDefaultState(getDefaultState().with(TARGET, BlockFace.WALL));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(TARGET, FACING));
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        for (Direction direction : pContext.getPlacementDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Axis.Y) {
                blockstate = getDefaultState().with(TARGET, direction == Direction.UP ? BlockFace.CEILING : BlockFace.FLOOR)
                    .with(FACING, pContext.getHorizontalPlayerFacing());
            } else {
                blockstate = getDefaultState().with(TARGET, BlockFace.WALL).with(FACING, direction.getOpposite());
            }

            return blockstate;
        }

        return null;
    }

    public static Direction getTargetDirection(BlockState pState) {
        return switch (pState.get(TARGET)) {
            case CEILING -> Direction.UP;
            case FLOOR -> Direction.DOWN;
            default -> pState.get(FACING);
        };
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == Axis.Y)
            return IWrenchable.super.getRotatedBlockState(originalState, targetedFace);

        Direction targetDirection = getTargetDirection(originalState);
        Direction newFacing = targetDirection.rotateClockwise(targetedFace.getAxis());
        if (targetedFace.getDirection() == AxisDirection.NEGATIVE)
            newFacing = newFacing.getOpposite();

        if (newFacing.getAxis() == Axis.Y)
            return originalState.with(TARGET, newFacing == Direction.UP ? BlockFace.CEILING : BlockFace.FLOOR);
        return originalState.with(TARGET, BlockFace.WALL).with(FACING, newFacing);
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
            return state.with(TARGET, newFacing == Direction.UP ? BlockFace.CEILING : BlockFace.FLOOR);
        return state.with(TARGET, BlockFace.WALL).with(FACING, newFacing);
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}
