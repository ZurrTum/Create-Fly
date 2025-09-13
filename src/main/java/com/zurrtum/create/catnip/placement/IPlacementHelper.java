package com.zurrtum.create.catnip.placement;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface IPlacementHelper {

    /**
     * used as an identifier in SuperGlueHandler to skip blocks placed by helpers
     */
    BlockState ID = new BlockState(Blocks.AIR, null, null);

    /**
     * @return a predicate that gets tested with the items held in the players hands<br>
     * should return true if this placement helper is active with the given item
     */
    Predicate<ItemStack> getItemPredicate();

    /**
     * @return a predicate that gets tested with the blockstate the player is looking at<br>
     * should return true if this placement helper is active with the given blockstate
     */
    Predicate<BlockState> getStatePredicate();

    /**
     * @param player the player that activated the placement helper
     * @param world  the world that the placement helper got activated in
     * @param state  the Blockstate of the Block that the player is looking at or clicked on
     * @param pos    the position of the Block the player is looking at or clicked on
     * @param ray    the exact raytrace result
     * @return the PlacementOffset object describing where to place the new block.<br>
     * Use {@link PlacementOffset#fail} when no new position could be found.<br>
     * Use {@link PlacementOffset#success(Vec3i)} with the new BlockPos to indicate a success
     * and call {@link PlacementOffset#withTransform(Function)} if the blocks default state has to be modified before it is placed
     */
    PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray);

    //sets the offset's ghost state with the default state of the held block item, this is used in PlacementHelpers and can be ignored in most cases
    default PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray, ItemStack heldItem) {
        PlacementOffset offset = getOffset(player, world, state, pos, ray);
        if (heldItem.getItem() instanceof BlockItem blockItem) {
            offset = offset.withGhostState(blockItem.getBlock().getDefaultState());
        }
        return offset;
    }

    static List<Direction> orderedByDistanceOnlyAxis(BlockPos pos, Vec3d hit, Direction.Axis axis) {
        return orderedByDistance(pos, hit, dir -> dir.getAxis() == axis);
    }

    static List<Direction> orderedByDistanceOnlyAxis(BlockPos pos, Vec3d hit, Direction.Axis axis, Predicate<Direction> includeDirection) {
        return orderedByDistance(pos, hit, ((Predicate<Direction>) dir -> dir.getAxis() == axis).and(includeDirection));
    }

    static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3d hit, Direction.Axis axis) {
        return orderedByDistance(pos, hit, dir -> dir.getAxis() != axis);
    }

    static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3d hit, Direction.Axis axis, Predicate<Direction> includeDirection) {
        return orderedByDistance(pos, hit, ((Predicate<Direction>) dir -> dir.getAxis() != axis).and(includeDirection));
    }

    static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3d hit, Direction.Axis first, Direction.Axis second) {
        return orderedByDistanceExceptAxis(pos, hit, first, d -> d.getAxis() != second);
    }

    static List<Direction> orderedByDistanceExceptAxis(
        BlockPos pos,
        Vec3d hit,
        Direction.Axis first,
        Direction.Axis second,
        Predicate<Direction> includeDirection
    ) {
        return orderedByDistanceExceptAxis(pos, hit, first, ((Predicate<Direction>) d -> d.getAxis() != second).and(includeDirection));
    }

    static List<Direction> orderedByDistance(BlockPos pos, Vec3d hit) {
        return orderedByDistance(pos, hit, _$ -> true);
    }

    static List<Direction> orderedByDistance(BlockPos pos, Vec3d hit, Predicate<Direction> includeDirection) {
        List<Direction> directions = new ArrayList<>();

        for (Direction dir : Iterate.directions) {
            if (includeDirection.test(dir)) {
                directions.add(dir);
            }
        }

        return orderedByDistance(pos, hit, directions);
    }

    static List<Direction> orderedByDistance(BlockPos pos, Vec3d hit, Collection<Direction> directions) {
        Vec3d centerToHit = hit.subtract(VecHelper.getCenterOf(pos));

        List<Pair<Direction, Double>> distances = new ArrayList<>();
        for (Direction dir : directions) {
            distances.add(Pair.of(dir, Vec3d.of(dir.getVector()).distanceTo(centerToHit)));
        }

        distances.sort(Comparator.comparingDouble(Pair::getSecond));

        List<Direction> sortedDirections = new ArrayList<>();
        for (Pair<Direction, Double> p : distances) {
            sortedDirections.add(p.getFirst());
        }

        return sortedDirections;
    }

    default boolean matchesItem(ItemStack item) {
        return getItemPredicate().test(item);
    }

    default boolean matchesState(BlockState state) {
        return getStatePredicate().test(state);
    }
}
