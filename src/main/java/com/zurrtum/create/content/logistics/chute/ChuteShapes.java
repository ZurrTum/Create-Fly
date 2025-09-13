package com.zurrtum.create.content.logistics.chute;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.logistics.chute.ChuteBlock.Shape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.HashMap;
import java.util.Map;

public class ChuteShapes {
    public static final VoxelShape INTERSECTION_MASK = Block.createCuboidShape(0, -16, 0, 16, 16, 16);
    public static final VoxelShape COLLISION_MASK = Block.createCuboidShape(0, 0, 0, 16, 24, 16);
    public static final VoxelShape PANEL = Block.createCuboidShape(1, -15, 0, 15, 4, 1);
    static Map<BlockState, VoxelShape> cache = new HashMap<>();
    static Map<BlockState, VoxelShape> collisionCache = new HashMap<>();

    public static VoxelShape createShape(BlockState state) {
        if (state.getBlock() == AllBlocks.SMART_CHUTE)
            return VoxelShapes.fullCube();

        Direction direction = state.get(ChuteBlock.FACING);
        Shape shape = state.get(ChuteBlock.SHAPE);

        boolean intersection = shape == Shape.INTERSECTION || shape == Shape.ENCASED;
        if (direction == Direction.DOWN)
            return intersection ? VoxelShapes.fullCube() : AllShapes.CHUTE;

        VoxelShape combineWith = intersection ? VoxelShapes.fullCube() : VoxelShapes.empty();
        VoxelShape result = VoxelShapes.union(combineWith, AllShapes.CHUTE_SLOPE.get(direction));
        if (intersection)
            result = VoxelShapes.combine(INTERSECTION_MASK, result, BooleanBiFunction.AND);
        return result;
    }

    public static VoxelShape getShape(BlockState state) {
        if (cache.containsKey(state))
            return cache.get(state);
        VoxelShape createdShape = createShape(state);
        cache.put(state, createdShape);
        return createdShape;
    }

    public static VoxelShape getCollisionShape(BlockState state) {
        if (collisionCache.containsKey(state))
            return collisionCache.get(state);
        VoxelShape createdShape = VoxelShapes.combine(COLLISION_MASK, getShape(state), BooleanBiFunction.AND);
        collisionCache.put(state, createdShape);
        return createdShape;
    }

    public static VoxelShape createSlope() {
        VoxelShape shape = VoxelShapes.empty();
        for (int i = 0; i < 16; i++) {
            float offset = i / 16f;
            shape = VoxelShapes.combineAndSimplify(shape, PANEL.offset(0, offset, offset), BooleanBiFunction.OR);
        }
        return shape;
    }
}
