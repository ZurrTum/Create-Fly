package com.zurrtum.create.content.decoration.girder;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GirderWrenchBehavior {
    @Nullable
    public static Pair<Direction, Action> getDirectionAndAction(BlockHitResult result, World world, BlockPos pos) {
        List<Pair<Direction, Action>> validDirections = getValidDirections(world, pos);

        if (validDirections.isEmpty())
            return null;

        List<Direction> directions = IPlacementHelper.orderedByDistance(pos, result.getPos(), validDirections.stream().map(Pair::getFirst).toList());

        if (directions.isEmpty())
            return null;

        Direction dir = directions.getFirst();
        return validDirections.stream().filter(pair -> pair.getFirst() == dir).findFirst().orElseGet(() -> Pair.of(dir, Action.SINGLE));
    }

    public static List<Pair<Direction, Action>> getValidDirections(BlockView level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);

        if (!blockState.isOf(AllBlocks.METAL_GIRDER))
            return Collections.emptyList();

        return Arrays.stream(Iterate.directions).<Pair<Direction, Action>>mapMulti((direction, consumer) -> {
            BlockState other = level.getBlockState(pos.offset(direction));

            if (!blockState.get(GirderBlock.X) && !blockState.get(GirderBlock.Z))
                return;

            // up and down
            if (direction.getAxis() == Axis.Y) {
                // no other girder in target dir
                if (!other.isOf(AllBlocks.METAL_GIRDER)) {
                    if (!blockState.get(GirderBlock.X) ^ !blockState.get(GirderBlock.Z))
                        consumer.accept(Pair.of(direction, Action.SINGLE));
                    return;
                }
                // this girder is a pole or cross
                if (blockState.get(GirderBlock.X) == blockState.get(GirderBlock.Z))
                    return;
                // other girder is a pole or cross
                if (other.get(GirderBlock.X) == other.get(GirderBlock.Z))
                    return;
                // toggle up/down connection for both
                consumer.accept(Pair.of(direction, Action.PAIR));

                return;
            }

            //					if (AllBlocks.METAL_GIRDER.has(other))
            //						consumer.accept(Pair.of(direction, Action.HORIZONTAL));

        }).toList();
    }

    public static boolean handleClick(World level, BlockPos pos, BlockState state, BlockHitResult result) {
        Pair<Direction, Action> dirPair = getDirectionAndAction(result, level, pos);
        if (dirPair == null)
            return false;
        if (level.isClient())
            return true;
        if (!state.get(GirderBlock.X) && !state.get(GirderBlock.Z))
            return false;

        Direction dir = dirPair.getFirst();

        BlockPos otherPos = pos.offset(dir);
        BlockState other = level.getBlockState(otherPos);

        if (dir == Direction.UP) {
            level.setBlockState(pos, postProcess(state.cycle(GirderBlock.TOP)), 2 | 16);
            if (dirPair.getSecond() == Action.PAIR && other.isOf(AllBlocks.METAL_GIRDER))
                level.setBlockState(otherPos, postProcess(other.cycle(GirderBlock.BOTTOM)), 2 | 16);
            return true;
        }

        if (dir == Direction.DOWN) {
            level.setBlockState(pos, postProcess(state.cycle(GirderBlock.BOTTOM)), 2 | 16);
            if (dirPair.getSecond() == Action.PAIR && other.isOf(AllBlocks.METAL_GIRDER))
                level.setBlockState(otherPos, postProcess(other.cycle(GirderBlock.TOP)), 2 | 16);
            return true;
        }

        //		if (dirPair.getSecond() == Action.HORIZONTAL) {
        //			BooleanProperty property = dir.getAxis() == Direction.Axis.X ? GirderBlock.X : GirderBlock.Z;
        //			level.setBlock(pos, state.cycle(property), 2 | 16);
        //
        //			return true;
        //		}

        return true;
    }

    private static BlockState postProcess(BlockState newState) {
        if (newState.get(GirderBlock.TOP) && newState.get(GirderBlock.BOTTOM))
            return newState;
        if (newState.get(GirderBlock.AXIS) != Axis.Y)
            return newState;
        return newState.with(GirderBlock.AXIS, newState.get(GirderBlock.X) ? Axis.X : Axis.Z);
    }

    public enum Action {
        SINGLE,
        PAIR,
        HORIZONTAL
    }
}
