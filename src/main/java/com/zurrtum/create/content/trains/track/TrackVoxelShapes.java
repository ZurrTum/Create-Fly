package com.zurrtum.create.content.trains.track;

import net.minecraft.block.Block;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class TrackVoxelShapes {
    public static VoxelShape orthogonal() {
        return Block.createCuboidShape(-14, 0, 0, 16 + 14, 4, 16);
    }

    public static VoxelShape longOrthogonalX() {
        return Block.createCuboidShape(-3.3, 0, -14, 19.3, 4, 16 + 14);
    }

    public static VoxelShape longOrthogonalZ() {
        return Block.createCuboidShape(-14, 0, -3.3, 16 + 14, 4, 19.3);
    }

    public static VoxelShape longOrthogonalZOffset() {
        return Block.createCuboidShape(-14, 0, 0, 16 + 14, 4, 24);
    }

    public static VoxelShape ascending() {
        VoxelShape shape = Block.createCuboidShape(-14, 0, 0, 16 + 14, 4, 4);
        VoxelShape[] shapes = new VoxelShape[6];
        for (int i = 0; i < 6; i++) {
            int off = (i + 1) * 2;
            shapes[i] = Block.createCuboidShape(-14, off, off, 16 + 14, 4 + off, 4 + off);
        }
        return VoxelShapes.union(shape, shapes);
    }

    public static VoxelShape diagonal() {
        VoxelShape shape = Block.createCuboidShape(0, 0, 0, 16, 4, 16);
        VoxelShape[] shapes = new VoxelShape[12];
        int off = 0;

        for (int i = 0; i < 6; i++) {
            off = (i + 1) * 2;
            shapes[i * 2] = Block.createCuboidShape(off, 0, off, 16 + off, 4, 16 + off);
            shapes[i * 2 + 1] = Block.createCuboidShape(-off, 0, -off, 16 - off, 4, 16 - off);
        }

        shape = VoxelShapes.union(shape, shapes);

        off = 10 * 2;
        shape = VoxelShapes.combineAndSimplify(shape, Block.createCuboidShape(off, 0, off, 16 + off, 4, 16 + off), BooleanBiFunction.ONLY_FIRST);
        shape = VoxelShapes.combineAndSimplify(shape, Block.createCuboidShape(-off, 0, -off, 16 - off, 4, 16 - off), BooleanBiFunction.ONLY_FIRST);

        off = 4 * 2;
        shape = VoxelShapes.union(shape, Block.createCuboidShape(off, 0, off, 16 + off, 4, 16 + off));
        shape = VoxelShapes.union(shape, Block.createCuboidShape(-off, 0, -off, 16 - off, 4, 16 - off));

        return shape.simplify();
    }
}
