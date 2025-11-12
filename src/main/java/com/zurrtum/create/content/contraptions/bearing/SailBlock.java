package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.foundation.utility.BlockHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SailBlock extends WrenchableDirectionalBlock {

    public static SailBlock frame(Properties properties) {
        return new SailBlock(properties, true, null);
    }

    public static Function<Properties, SailBlock> withCanvas(DyeColor color) {
        return properties -> new SailBlock(properties, false, color);
    }

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    protected final boolean frame;
    protected final DyeColor color;

    protected SailBlock(Properties properties, boolean frame, DyeColor color) {
        super(properties);
        this.frame = frame;
        this.color = color;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state.setValue(FACING, state.getValue(FACING).getOpposite());
    }


    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);
                return InteractionResult.SUCCESS;
            }
        }

        if (stack.getItem() instanceof ShearsItem) {
            if (!level.isClientSide())
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
            applyDye(state, level, pos, hitResult.getLocation(), null);
            return InteractionResult.SUCCESS;
        }

        if (frame)
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        DyeColor color = AllItemTags.getDyeColor(stack);
        if (color != null) {
            if (!level.isClientSide())
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.1f - level.random.nextFloat() * .2f);
            applyDye(state, level, pos, hitResult.getLocation(), color);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    public SailBlock getColorBlock(DyeColor color) {
        return switch (color) {
            case ORANGE -> AllBlocks.ORANGE_SAIL;
            case MAGENTA -> AllBlocks.MAGENTA_SAIL;
            case LIGHT_BLUE -> AllBlocks.LIGHT_BLUE_SAIL;
            case YELLOW -> AllBlocks.YELLOW_SAIL;
            case LIME -> AllBlocks.LIME_SAIL;
            case PINK -> AllBlocks.PINK_SAIL;
            case GRAY -> AllBlocks.GRAY_SAIL;
            case LIGHT_GRAY -> AllBlocks.LIGHT_GRAY_SAIL;
            case CYAN -> AllBlocks.CYAN_SAIL;
            case PURPLE -> AllBlocks.PURPLE_SAIL;
            case BLUE -> AllBlocks.BLUE_SAIL;
            case BROWN -> AllBlocks.BROWN_SAIL;
            case GREEN -> AllBlocks.GREEN_SAIL;
            case RED -> AllBlocks.RED_SAIL;
            case BLACK -> AllBlocks.BLACK_SAIL;
            default -> AllBlocks.SAIL;
        };
    }

    public void applyDye(BlockState state, Level world, BlockPos pos, Vec3 hit, @Nullable DyeColor color) {
        BlockState newState = (color == null ? AllBlocks.SAIL_FRAME : getColorBlock(color)).defaultBlockState();
        newState = BlockHelper.copyProperties(state, newState);

        // Dye the block itself
        if (state != newState) {
            world.setBlockAndUpdate(pos, newState);
            return;
        }

        // Dye all adjacent
        List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, hit, state.getValue(FACING).getAxis());
        for (Direction d : directions) {
            BlockPos offset = pos.relative(d);
            BlockState adjacentState = world.getBlockState(offset);
            Block block = adjacentState.getBlock();
            if (!(block instanceof SailBlock) || ((SailBlock) block).frame)
                continue;
            if (state.getValue(FACING) != adjacentState.getValue(FACING))
                continue;
            if (state == adjacentState)
                continue;
            world.setBlockAndUpdate(offset, newState);
            return;
        }

        // Dye all the things
        List<BlockPos> frontier = new ArrayList<>();
        frontier.add(pos);
        Set<BlockPos> visited = new HashSet<>();
        int timeout = 100;
        while (!frontier.isEmpty()) {
            if (timeout-- < 0)
                break;

            BlockPos currentPos = frontier.remove(0);
            visited.add(currentPos);

            for (Direction d : Iterate.directions) {
                if (d.getAxis() == state.getValue(FACING).getAxis())
                    continue;
                BlockPos offset = currentPos.relative(d);
                if (visited.contains(offset))
                    continue;
                BlockState adjacentState = world.getBlockState(offset);
                Block block = adjacentState.getBlock();
                if (!(block instanceof SailBlock) || ((SailBlock) block).frame && color != null)
                    continue;
                if (adjacentState.getValue(FACING) != state.getValue(FACING))
                    continue;
                if (state != adjacentState)
                    world.setBlockAndUpdate(offset, newState);
                frontier.add(offset);
                visited.add(offset);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return (frame ? AllShapes.SAIL_FRAME : AllShapes.SAIL).get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter p_220071_2_, BlockPos p_220071_3_, CollisionContext p_220071_4_) {
        return (frame ? AllShapes.SAIL_FRAME_COLLISION : AllShapes.SAIL).get(state.getValue(FACING));
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack pickBlock = super.getCloneItemStack(world, pos, state, includeData);
        if (pickBlock.isEmpty())
            return AllBlocks.SAIL.getCloneItemStack(world, pos, state, includeData);
        return pickBlock;
    }

    @Override
    public void fallOn(Level p_152426_, BlockState p_152427_, BlockPos p_152428_, Entity p_152429_, double p_152430_) {
        if (frame)
            super.fallOn(p_152426_, p_152427_, p_152428_, p_152429_, p_152430_);
        super.fallOn(p_152426_, p_152427_, p_152428_, p_152429_, 0);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter p_176216_1_, Entity p_176216_2_) {
        if (frame || p_176216_2_.isSuppressingBounce()) {
            super.updateEntityMovementAfterFallOn(p_176216_1_, p_176216_2_);
        } else {
            this.bounce(p_176216_2_);
        }
    }

    private void bounce(Entity p_226860_1_) {
        Vec3 Vector3d = p_226860_1_.getDeltaMovement();
        if (Vector3d.y < 0.0D) {
            double d0 = p_226860_1_ instanceof LivingEntity ? 1.0D : 0.8D;
            p_226860_1_.setDeltaMovement(Vector3d.x, -Vector3d.y * (double) 0.26F * d0, Vector3d.z);
        }

    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    public boolean isFrame() {
        return frame;
    }

    public DyeColor getColor() {
        return color;
    }

    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.is(AllItems.SAIL) || stack.is(AllItems.SAIL_FRAME);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.getBlock() instanceof SailBlock;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(
                pos,
                ray.getLocation(),
                state.getValue(SailBlock.FACING).getAxis(),
                dir -> world.getBlockState(pos.relative(dir)).canBeReplaced()
            );

            if (directions.isEmpty())
                return PlacementOffset.fail();
            else {
                return PlacementOffset.success(pos.relative(directions.get(0)), s -> s.setValue(FACING, state.getValue(FACING)));
            }
        }
    }
}
