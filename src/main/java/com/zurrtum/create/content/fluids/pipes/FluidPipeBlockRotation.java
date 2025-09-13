package com.zurrtum.create.content.fluids.pipes;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

import java.util.Map;

public class FluidPipeBlockRotation {

    public static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = ConnectingBlock.FACING_PROPERTIES;

    public static BlockState rotate(BlockState state, BlockRotation rotation) {
        BlockState rotated = state;
        for (Direction direction : Iterate.horizontalDirections)
            rotated = rotated.with(FACING_TO_PROPERTY_MAP.get(rotation.rotate(direction)),
                state.get(FACING_TO_PROPERTY_MAP.get(direction)));
        return rotated;
    }

    public static BlockState mirror(BlockState state, BlockMirror mirror) {
        BlockState mirrored = state;
        for (Direction direction : Iterate.horizontalDirections)
            mirrored = mirrored.with(FACING_TO_PROPERTY_MAP.get(mirror.apply(direction)),
                state.get(FACING_TO_PROPERTY_MAP.get(direction)));
        return mirrored;
    }

    public static BlockState transform(BlockState state, StructureTransform transform) {
        if (transform.mirror != null)
            state = mirror(state, transform.mirror);

        if (transform.rotationAxis == Direction.Axis.Y)
            return rotate(state, transform.rotation);

        BlockState rotated = state;
        for (Direction direction : Iterate.directions)
            rotated = rotated.with(FACING_TO_PROPERTY_MAP.get(transform.rotateFacing(direction)),
                state.get(FACING_TO_PROPERTY_MAP.get(direction)));
        return rotated;
    }

}
