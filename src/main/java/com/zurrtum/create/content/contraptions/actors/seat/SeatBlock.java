package com.zurrtum.create.content.contraptions.actors.seat;

import com.google.common.base.Optional;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllEntityTags;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Function;

public class SeatBlock extends Block implements ProperWaterloggedBlock {

    protected final DyeColor color;

    public SeatBlock(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    public static Function<Properties, SeatBlock> dyed(DyeColor color) {
        return properties -> new SeatBlock(properties, color);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(WATERLOGGED));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return withWater(super.getStateForPlacement(pContext), pContext);
    }

    @Override
    public BlockState updateShape(
        BlockState pState,
        LevelReader pLevel,
        ScheduledTickAccess tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        RandomSource random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public void fallOn(Level p_152426_, BlockState p_152427_, BlockPos p_152428_, Entity p_152429_, double p_152430_) {
        super.fallOn(p_152426_, p_152427_, p_152428_, p_152429_, p_152430_ * 0.5F);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter reader, Entity entity) {
        BlockPos pos = entity.blockPosition();
        if (entity instanceof Player || !(entity instanceof LivingEntity) || !canBePickedUp(entity) || isSeatOccupied(entity.level(), pos)) {
            if (entity.isSuppressingBounce()) {
                super.updateEntityMovementAfterFallOn(reader, entity);
                return;
            }

            Vec3 vec3 = entity.getDeltaMovement();
            if (vec3.y < 0.0D) {
                double d0 = entity instanceof LivingEntity ? 1.0D : 0.8D;
                entity.setDeltaMovement(vec3.x, -vec3.y * (double) 0.66F * d0, vec3.z);
            }

            return;
        }
        if (reader.getBlockState(pos).getBlock() != this)
            return;
        if (entity instanceof Leashable leashable && leashable.isLeashed())
            return;
        sitDown(entity.level(), pos, entity);
    }

    //TODO
    //    @Override
    //    public PathType getBlockPathType(BlockState state, BlockView world, BlockPos pos, @Nullable Mob entity) {
    //        return PathType.RAIL;
    //    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return AllShapes.SEAT;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockGetter p_220071_2_, BlockPos p_220071_3_, CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext ecc && ecc.getEntity() instanceof Player)
            return AllShapes.SEAT_COLLISION_PLAYERS;
        return AllShapes.SEAT_COLLISION;
    }

    public static SeatBlock getColorBlock(DyeColor color) {
        return switch (color) {
            case WHITE -> AllBlocks.WHITE_SEAT;
            case ORANGE -> AllBlocks.ORANGE_SEAT;
            case MAGENTA -> AllBlocks.MAGENTA_SEAT;
            case LIGHT_BLUE -> AllBlocks.LIGHT_BLUE_SEAT;
            case YELLOW -> AllBlocks.YELLOW_SEAT;
            case LIME -> AllBlocks.LIME_SEAT;
            case PINK -> AllBlocks.PINK_SEAT;
            case GRAY -> AllBlocks.GRAY_SEAT;
            case LIGHT_GRAY -> AllBlocks.LIGHT_GRAY_SEAT;
            case CYAN -> AllBlocks.CYAN_SEAT;
            case PURPLE -> AllBlocks.PURPLE_SEAT;
            case BLUE -> AllBlocks.BLUE_SEAT;
            case BROWN -> AllBlocks.BROWN_SEAT;
            case GREEN -> AllBlocks.GREEN_SEAT;
            case RED -> AllBlocks.RED_SEAT;
            case BLACK -> AllBlocks.BLACK_SEAT;
        };
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
        if (player.isShiftKeyDown() || FakePlayerHandler.has(player))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        DyeColor color = AllItemTags.getDyeColor(stack);
        if (color != null && color != this.color) {
            if (level.isClientSide())
                return InteractionResult.SUCCESS;
            BlockState newState = BlockHelper.copyProperties(state, getColorBlock(color).defaultBlockState());
            level.setBlockAndUpdate(pos, newState);
            return InteractionResult.SUCCESS;
        }

        List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
        if (!seats.isEmpty()) {
            SeatEntity seatEntity = seats.getFirst();
            List<Entity> passengers = seatEntity.getPassengers();
            if (!passengers.isEmpty() && passengers.getFirst() instanceof Player)
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            if (!level.isClientSide()) {
                seatEntity.ejectPassengers();
                player.startRiding(seatEntity);
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        Optional<Entity> leashed = getLeashed(level, player);
        if (leashed.isPresent()) {
            ((Leashable) leashed.get()).removeLeash();
            Inventory playerInventory = player.getInventory();
            if (!player.isCreative() || playerInventory.findSlotMatchingItem(stack) == -1) {
                playerInventory.placeItemBackInInventory(new ItemStack(Items.LEAD));
            }
        }
        sitDown(level, pos, leashed.or(player));
        return InteractionResult.SUCCESS;
    }

    public static boolean isSeatOccupied(Level world, BlockPos pos) {
        return !world.getEntitiesOfClass(SeatEntity.class, new AABB(pos)).isEmpty();
    }

    public static Optional<Entity> getLeashed(Level level, Player player) {
        List<Entity> entities = player.level().getEntities((Entity) null, player.getBoundingBox().inflate(10), e -> true);
        for (Entity e : entities)
            if (e instanceof Mob mob && mob.getLeashHolder() == player && SeatBlock.canBePickedUp(e))
                return Optional.of(mob);
        return Optional.absent();
    }

    public static boolean canBePickedUp(Entity passenger) {
        if (passenger instanceof Shulker)
            return false;
        if (passenger instanceof Player)
            return false;
        if (passenger.getType().is(AllEntityTags.IGNORE_SEAT))
            return false;
        if (!AllConfigs.server().logistics.seatHostileMobs.get() && !passenger.getType().getCategory().isFriendly())
            return false;
        return passenger instanceof LivingEntity;
    }

    public static void sitDown(Level world, BlockPos pos, Entity entity) {
        if (world.isClientSide())
            return;
        SeatEntity seat = new SeatEntity(world, pos);
        seat.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        world.addFreshEntity(seat);
        entity.startRiding(seat, true, true);
        if (entity instanceof TamableAnimal ta)
            ta.setInSittingPose(true);
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

}
