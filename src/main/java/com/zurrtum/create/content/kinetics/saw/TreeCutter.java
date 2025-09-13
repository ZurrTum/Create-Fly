package com.zurrtum.create.content.kinetics.saw;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.foundation.utility.AbstractBlockBreakQueue;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TreeCutter {

    public static final Tree NO_TREE = new Tree(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

    //TODO
    //    public static boolean canDynamicTreeCutFrom(Block startBlock) {
    //        return Mods.DYNAMICTREES.runIfInstalled(() -> () -> DynamicTree.isDynamicBranch(startBlock)).orElse(false);
    //    }
    //
    //    @Nonnull
    //    public static Optional<AbstractBlockBreakQueue> findDynamicTree(Block startBlock, BlockPos pos) {
    //        if (canDynamicTreeCutFrom(startBlock))
    //            return Mods.DYNAMICTREES.runIfInstalled(() -> () -> new DynamicTree(pos));
    //        return Optional.empty();
    //    }

    /**
     * Finds a tree at the given pos. Block at the position should be air
     *
     * @param reader      the level that will be searched for a tree
     * @param pos         position that the saw cut at
     * @param brokenState block state what was broken by the saw
     */
    public static Tree findTree(@Nullable BlockView reader, BlockPos pos, BlockState brokenState) {
        if (reader == null)
            return NO_TREE;

        List<BlockPos> logs = new ArrayList<>();
        List<BlockPos> leaves = new ArrayList<>();
        List<BlockPos> attachments = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> frontier = new LinkedList<>();

        BlockState stateAbove = reader.getBlockState(pos.up());
        // Bamboo, Sugar Cane, Cactus
        if (isVerticalPlant(brokenState)) {
            if (!isVerticalPlant(stateAbove))
                return NO_TREE;

            logs.add(pos.up());
            for (int i = 1; i < reader.getHeight(); i++) {
                BlockPos current = pos.up(i);
                if (!isVerticalPlant(reader.getBlockState(current)))
                    break;
                logs.add(current);
            }
            Collections.reverse(logs);
            return new Tree(logs, leaves, attachments);
        }

        // Chorus
        if (isChorus(brokenState)) {
            if (!isChorus(stateAbove))
                return NO_TREE;

            frontier.add(pos.up());
            while (!frontier.isEmpty()) {
                BlockPos current = frontier.remove(0);
                visited.add(current);
                logs.add(current);
                for (Direction direction : Iterate.directions) {
                    BlockPos offset = current.offset(direction);
                    if (visited.contains(offset))
                        continue;
                    if (!isChorus(reader.getBlockState(offset)))
                        continue;
                    frontier.add(offset);
                }
            }
            Collections.reverse(logs);
            return new Tree(logs, leaves, attachments);
        }

        // Regular Tree
        if (!validateCut(reader, pos))
            return NO_TREE;

        visited.add(pos);
        BlockPos.stream(pos.add(-1, 0, -1), pos.add(1, 1, 1)).forEach(p -> frontier.add(new BlockPos(p)));

        // Find all logs & roots
        boolean hasRoots = false;
        while (!frontier.isEmpty()) {
            BlockPos currentPos = frontier.remove(0);
            if (!visited.add(currentPos))
                continue;

            BlockState currentState = reader.getBlockState(currentPos);
            if (isRoot(currentState))
                hasRoots = true;
            else if (!isLog(currentState))
                continue;
            logs.add(currentPos);
            forNeighbours(currentPos, visited, SearchDirection.UP, p -> frontier.add(new BlockPos(p)));
        }

        visited.clear();
        visited.addAll(logs);
        frontier.addAll(logs);

        if (hasRoots) {
            while (!frontier.isEmpty()) {
                BlockPos currentPos = frontier.remove(0);
                if (!logs.contains(currentPos) && !visited.add(currentPos))
                    continue;

                BlockState currentState = reader.getBlockState(currentPos);
                if (!isRoot(currentState))
                    continue;
                logs.add(currentPos);
                forNeighbours(currentPos, visited, SearchDirection.DOWN, p -> frontier.add(new BlockPos(p)));
            }

            visited.clear();
            visited.addAll(logs);
            frontier.addAll(logs);
        }

        // Find all leaves
        while (!frontier.isEmpty()) {
            BlockPos prevPos = frontier.remove(0);
            if (!logs.contains(prevPos) && !visited.add(prevPos))
                continue;

            BlockState prevState = reader.getBlockState(prevPos);
            int prevLeafDistance = isLeaf(prevState) ? getLeafDistance(prevState) : 0;

            forNeighbours(
                prevPos, visited, SearchDirection.BOTH, currentPos -> {
                    BlockState state = reader.getBlockState(currentPos);
                    BlockPos subtract = currentPos.subtract(pos);
                    BlockPos currentPosImmutable = currentPos.toImmutable();

                    if (state.isIn(AllBlockTags.TREE_ATTACHMENTS)) {
                        attachments.add(currentPosImmutable);
                        visited.add(currentPosImmutable);
                        return;
                    }

                    int horizontalDistance = Math.max(Math.abs(subtract.getX()), Math.abs(subtract.getZ()));
                    if (horizontalDistance <= nonDecayingLeafDistance(state)) {
                        leaves.add(currentPosImmutable);
                        frontier.add(currentPosImmutable);
                        return;
                    }

                    if (isLeaf(state) && getLeafDistance(state) > prevLeafDistance) {
                        leaves.add(currentPosImmutable);
                        frontier.add(currentPosImmutable);
                        return;
                    }

                }
            );
        }

        return new Tree(logs, leaves, attachments);
    }

    private static int getLeafDistance(BlockState state) {
        IntProperty distanceProperty = LeavesBlock.DISTANCE;
        for (Property<?> property : state.getEntries().keySet())
            if (property instanceof IntProperty ip && property.getName().equals("distance"))
                distanceProperty = ip;
        return state.get(distanceProperty);
    }

    public static boolean isChorus(BlockState stateAbove) {
        return stateAbove.getBlock() instanceof ChorusPlantBlock || stateAbove.getBlock() instanceof ChorusFlowerBlock;
    }

    public static boolean isVerticalPlant(BlockState stateAbove) {
        Block block = stateAbove.getBlock();
        if (block instanceof BambooBlock)
            return true;
        if (block instanceof CactusBlock)
            return true;
        if (block instanceof SugarCaneBlock)
            return true;
        if (block instanceof KelpPlantBlock)
            return true;
        return block instanceof KelpBlock;
    }

    /**
     * Checks whether a tree was fully cut by seeing whether the layer above the cut
     * is not supported by any more logs.
     *
     * @param reader
     * @param pos
     * @return
     */
    private static boolean validateCut(BlockView reader, BlockPos pos) {
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> frontier = new LinkedList<>();
        frontier.add(pos);
        frontier.add(pos.up());
        int posY = pos.getY();

        while (!frontier.isEmpty()) {
            BlockPos currentPos = frontier.remove(0);
            BlockPos belowPos = currentPos.down();

            visited.add(currentPos);
            boolean lowerLayer = currentPos.getY() == posY;

            BlockState currentState = reader.getBlockState(currentPos);
            BlockState belowState = reader.getBlockState(belowPos);

            if (!isLog(currentState) && !isRoot(currentState))
                continue;
            if (!lowerLayer && !pos.equals(belowPos) && (isLog(belowState) || isRoot(belowState)))
                return false;

            for (Direction direction : Iterate.directions) {
                if (direction == Direction.DOWN)
                    continue;
                if (direction == Direction.UP && !lowerLayer)
                    continue;
                BlockPos offset = currentPos.offset(direction);
                if (visited.contains(offset))
                    continue;
                frontier.add(offset);
            }

        }

        return true;
    }

    private enum SearchDirection {
        UP(0, 1),
        DOWN(-1, 0),
        BOTH(-1, 1);

        int minY;
        int maxY;

        private SearchDirection(int minY, int maxY) {
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    private static void forNeighbours(BlockPos pos, Set<BlockPos> visited, SearchDirection direction, Consumer<BlockPos> acceptor) {
        BlockPos.stream(pos.add(-1, direction.minY, -1), pos.add(1, direction.maxY, 1)).filter(((Predicate<BlockPos>) visited::contains).negate())
            .forEach(acceptor);
    }

    public static boolean isRoot(BlockState state) {
        return state.isIn(AllBlockTags.ROOTS);
    }

    public static boolean isLog(BlockState state) {
        return state.isIn(BlockTags.LOGS) || state.isIn(AllBlockTags.SLIMY_LOGS) || state.isOf(Blocks.MUSHROOM_STEM);
    }

    private static int nonDecayingLeafDistance(BlockState state) {
        if (state.isOf(Blocks.RED_MUSHROOM_BLOCK))
            return 2;
        if (state.isOf(Blocks.BROWN_MUSHROOM_BLOCK))
            return 3;
        if (state.isIn(BlockTags.WART_BLOCKS) || state.isOf(Blocks.WEEPING_VINES) || state.isOf(Blocks.WEEPING_VINES_PLANT))
            return 3;
        return -1;
    }

    private static boolean isLeaf(BlockState state) {
        for (Property<?> property : state.getEntries().keySet())
            if (property instanceof IntProperty && property.getName().equals("distance") && property != Properties.DISTANCE_0_7)
                return true;
        return false;
    }

    public static class Tree extends AbstractBlockBreakQueue {
        private final List<BlockPos> logs;
        private final List<BlockPos> leaves;
        private final List<BlockPos> attachments;

        public Tree(List<BlockPos> logs, List<BlockPos> leaves, List<BlockPos> attachments) {
            this.logs = logs;
            this.leaves = leaves;
            this.attachments = attachments;
        }

        @Override
        public void destroyBlocks(World world, ItemStack toDamage, @Nullable PlayerEntity playerEntity, BiConsumer<BlockPos, ItemStack> drop) {
            attachments.forEach(makeCallbackFor(world, 1 / 32f, toDamage, playerEntity, drop));
            logs.forEach(makeCallbackFor(world, 1 / 2f, toDamage, playerEntity, drop));
            leaves.forEach(makeCallbackFor(world, 1 / 8f, toDamage, playerEntity, drop));
        }
    }
}
