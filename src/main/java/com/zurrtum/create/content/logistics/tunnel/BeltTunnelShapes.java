package com.zurrtum.create.content.logistics.tunnel;

import com.zurrtum.create.catnip.math.VoxelShaper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class BeltTunnelShapes {

    private static final VoxelShape block = Block.createCuboidShape(0, -5, 0, 16, 16, 16);

    private static final VoxelShaper opening = VoxelShaper.forHorizontal(Block.createCuboidShape(2, -5, 14, 14, 10, 16), Direction.SOUTH);

    private static final VoxelShaper STRAIGHT = VoxelShaper.forHorizontalAxis(
        VoxelShapes.combineAndSimplify(
            block,
            VoxelShapes.union(opening.get(Direction.SOUTH), opening.get(Direction.NORTH)),
            BooleanBiFunction.NOT_SAME
        ), Axis.Z
    ),

    TEE = VoxelShaper.forHorizontal(
        VoxelShapes.combineAndSimplify(
            block,
            VoxelShapes.union(opening.get(Direction.NORTH), opening.get(Direction.WEST), opening.get(Direction.EAST)),
            BooleanBiFunction.NOT_SAME
        ), Direction.SOUTH
    );

    private static final VoxelShape CROSS = VoxelShapes.combineAndSimplify(
        block,
        VoxelShapes.union(opening.get(Direction.SOUTH), opening.get(Direction.NORTH), opening.get(Direction.WEST), opening.get(Direction.EAST)),
        BooleanBiFunction.NOT_SAME
    );

    public static VoxelShape getShape(BlockState state) {
        BeltTunnelBlock.Shape shape = state.get(BeltTunnelBlock.SHAPE);
        Direction.Axis axis = state.get(BeltTunnelBlock.HORIZONTAL_AXIS);

        if (shape == BeltTunnelBlock.Shape.CROSS)
            return CROSS;

        if (BeltTunnelBlock.isStraight(state))
            return STRAIGHT.get(axis);

        if (shape == BeltTunnelBlock.Shape.T_LEFT)
            return TEE.get(axis == Direction.Axis.Z ? Direction.EAST : Direction.NORTH);

        if (shape == BeltTunnelBlock.Shape.T_RIGHT)
            return TEE.get(axis == Direction.Axis.Z ? Direction.WEST : Direction.SOUTH);

        // something went wrong
        return VoxelShapes.fullCube();
    }
}
