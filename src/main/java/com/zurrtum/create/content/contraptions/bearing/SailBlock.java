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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class SailBlock extends WrenchableDirectionalBlock {

    public static SailBlock frame(Settings properties) {
        return new SailBlock(properties, true, null);
    }

    public static Function<Settings, SailBlock> withCanvas(DyeColor color) {
        return properties -> new SailBlock(properties, false, color);
    }

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    protected final boolean frame;
    protected final DyeColor color;

    protected SailBlock(Settings properties, boolean frame, DyeColor color) {
        super(properties);
        this.frame = frame;
        this.color = color;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        return state.with(FACING, state.get(FACING).getOpposite());
    }


    @Override
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isSneaking() && player.canModifyBlocks()) {
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);
                return ActionResult.SUCCESS;
            }
        }

        if (stack.getItem() instanceof ShearsItem) {
            if (!level.isClient)
                level.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.BLOCKS, 1.0f, 1.0f);
            applyDye(state, level, pos, hitResult.getPos(), null);
            return ActionResult.SUCCESS;
        }

        if (frame)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        DyeColor color = AllItemTags.getDyeColor(stack);
        if (color != null) {
            if (!level.isClient)
                level.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0f, 1.1f - level.random.nextFloat() * .2f);
            applyDye(state, level, pos, hitResult.getPos(), color);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
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

    public void applyDye(BlockState state, World world, BlockPos pos, Vec3d hit, @Nullable DyeColor color) {
        BlockState newState = (color == null ? AllBlocks.SAIL_FRAME : getColorBlock(color)).getDefaultState();
        newState = BlockHelper.copyProperties(state, newState);

        // Dye the block itself
        if (state != newState) {
            world.setBlockState(pos, newState);
            return;
        }

        // Dye all adjacent
        List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, hit, state.get(FACING).getAxis());
        for (Direction d : directions) {
            BlockPos offset = pos.offset(d);
            BlockState adjacentState = world.getBlockState(offset);
            Block block = adjacentState.getBlock();
            if (!(block instanceof SailBlock) || ((SailBlock) block).frame)
                continue;
            if (state.get(FACING) != adjacentState.get(FACING))
                continue;
            if (state == adjacentState)
                continue;
            world.setBlockState(offset, newState);
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
                if (d.getAxis() == state.get(FACING).getAxis())
                    continue;
                BlockPos offset = currentPos.offset(d);
                if (visited.contains(offset))
                    continue;
                BlockState adjacentState = world.getBlockState(offset);
                Block block = adjacentState.getBlock();
                if (!(block instanceof SailBlock) || ((SailBlock) block).frame && color != null)
                    continue;
                if (adjacentState.get(FACING) != state.get(FACING))
                    continue;
                if (state != adjacentState)
                    world.setBlockState(offset, newState);
                frontier.add(offset);
                visited.add(offset);
            }
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return (frame ? AllShapes.SAIL_FRAME : AllShapes.SAIL).get(state.get(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView p_220071_2_, BlockPos p_220071_3_, ShapeContext p_220071_4_) {
        return (frame ? AllShapes.SAIL_FRAME_COLLISION : AllShapes.SAIL).get(state.get(FACING));
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack pickBlock = super.getPickStack(world, pos, state, includeData);
        if (pickBlock.isEmpty())
            return AllBlocks.SAIL.getPickStack(world, pos, state, includeData);
        return pickBlock;
    }

    @Override
    public void onLandedUpon(World p_152426_, BlockState p_152427_, BlockPos p_152428_, Entity p_152429_, double p_152430_) {
        if (frame)
            super.onLandedUpon(p_152426_, p_152427_, p_152428_, p_152429_, p_152430_);
        super.onLandedUpon(p_152426_, p_152427_, p_152428_, p_152429_, 0);
    }

    @Override
    public void onEntityLand(BlockView p_176216_1_, Entity p_176216_2_) {
        if (frame || p_176216_2_.bypassesLandingEffects()) {
            super.onEntityLand(p_176216_1_, p_176216_2_);
        } else {
            this.bounce(p_176216_2_);
        }
    }

    private void bounce(Entity p_226860_1_) {
        Vec3d Vector3d = p_226860_1_.getVelocity();
        if (Vector3d.y < 0.0D) {
            double d0 = p_226860_1_ instanceof LivingEntity ? 1.0D : 0.8D;
            p_226860_1_.setVelocity(Vector3d.x, -Vector3d.y * (double) 0.26F * d0, Vector3d.z);
        }

    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
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
            return stack -> stack.isOf(AllItems.SAIL) || stack.isOf(AllItems.SAIL_FRAME);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.getBlock() instanceof SailBlock;
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(
                pos,
                ray.getPos(),
                state.get(SailBlock.FACING).getAxis(),
                dir -> world.getBlockState(pos.offset(dir)).isReplaceable()
            );

            if (directions.isEmpty())
                return PlacementOffset.fail();
            else {
                return PlacementOffset.success(pos.offset(directions.get(0)), s -> s.with(FACING, state.get(FACING)));
            }
        }
    }
}
