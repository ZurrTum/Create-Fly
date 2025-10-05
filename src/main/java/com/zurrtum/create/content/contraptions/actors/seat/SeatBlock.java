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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.List;
import java.util.function.Function;

public class SeatBlock extends Block implements ProperWaterloggedBlock {

    protected final DyeColor color;

    public SeatBlock(Settings properties, DyeColor color) {
        super(properties);
        this.color = color;
        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    public static Function<Settings, SeatBlock> dyed(DyeColor color) {
        return properties -> new SeatBlock(properties, color);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(WATERLOGGED));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        return withWater(super.getPlacementState(pContext), pContext);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public void onLandedUpon(World p_152426_, BlockState p_152427_, BlockPos p_152428_, Entity p_152429_, double p_152430_) {
        super.onLandedUpon(p_152426_, p_152427_, p_152428_, p_152429_, p_152430_ * 0.5F);
    }

    @Override
    public void onEntityLand(BlockView reader, Entity entity) {
        BlockPos pos = entity.getBlockPos();
        if (entity instanceof PlayerEntity || !(entity instanceof LivingEntity) || !canBePickedUp(entity) || isSeatOccupied(
            entity.getEntityWorld(),
            pos
        )) {
            if (entity.bypassesLandingEffects()) {
                super.onEntityLand(reader, entity);
                return;
            }

            Vec3d vec3 = entity.getVelocity();
            if (vec3.y < 0.0D) {
                double d0 = entity instanceof LivingEntity ? 1.0D : 0.8D;
                entity.setVelocity(vec3.x, -vec3.y * (double) 0.66F * d0, vec3.z);
            }

            return;
        }
        if (reader.getBlockState(pos).getBlock() != this)
            return;
        if (entity instanceof Leashable leashable && leashable.isLeashed())
            return;
        sitDown(entity.getEntityWorld(), pos, entity);
    }

    //TODO
    //    @Override
    //    public PathType getBlockPathType(BlockState state, BlockView world, BlockPos pos, @Nullable Mob entity) {
    //        return PathType.RAIL;
    //    }

    @Override
    public VoxelShape getOutlineShape(BlockState p_220053_1_, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return AllShapes.SEAT;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockView p_220071_2_, BlockPos p_220071_3_, ShapeContext ctx) {
        if (ctx instanceof EntityShapeContext ecc && ecc.getEntity() instanceof PlayerEntity)
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
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        if (player.isSneaking() || FakePlayerHandler.has(player))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        DyeColor color = AllItemTags.getDyeColor(stack);
        if (color != null && color != this.color) {
            if (level.isClient())
                return ActionResult.SUCCESS;
            BlockState newState = BlockHelper.copyProperties(state, getColorBlock(color).getDefaultState());
            level.setBlockState(pos, newState);
            return ActionResult.SUCCESS;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        List<SeatEntity> seats = level.getNonSpectatingEntities(SeatEntity.class, new Box(x, y - 0.1f, z, x + 1, y + 1, z + 1));
        if (!seats.isEmpty()) {
            SeatEntity seatEntity = seats.getFirst();
            List<Entity> passengers = seatEntity.getPassengerList();
            if (!passengers.isEmpty() && passengers.getFirst() instanceof PlayerEntity)
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            if (!level.isClient()) {
                seatEntity.removeAllPassengers();
                player.startRiding(seatEntity);
            }
            return ActionResult.SUCCESS;
        }

        if (level.isClient())
            return ActionResult.SUCCESS;
        Optional<Entity> leashed = getLeashed(level, player);
        if (leashed.isPresent()) {
            ((Leashable) leashed.get()).detachLeashWithoutDrop();
            PlayerInventory playerInventory = player.getInventory();
            if (!player.isCreative() || playerInventory.getSlotWithStack(stack) == -1) {
                playerInventory.offerOrDrop(new ItemStack(Items.LEAD));
            }
        }
        sitDown(level, pos, leashed.or(player));
        return ActionResult.SUCCESS;
    }

    public static boolean isSeatOccupied(World world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return !world.getNonSpectatingEntities(SeatEntity.class, new Box(x, y - 0.1f, z, x + 1, y + 1, z + 1)).isEmpty();
    }

    public static Optional<Entity> getLeashed(World level, PlayerEntity player) {
        List<Entity> entities = player.getEntityWorld().getOtherEntities(null, player.getBoundingBox().expand(10), e -> true);
        for (Entity e : entities)
            if (e instanceof MobEntity mob && mob.getLeashHolder() == player && SeatBlock.canBePickedUp(e))
                return Optional.of(mob);
        return Optional.absent();
    }

    public static boolean canBePickedUp(Entity passenger) {
        if (passenger instanceof ShulkerEntity)
            return false;
        if (passenger instanceof PlayerEntity)
            return false;
        if (passenger.getType().isIn(AllEntityTags.IGNORE_SEAT))
            return false;
        if (!AllConfigs.server().logistics.seatHostileMobs.get() && !passenger.getType().getSpawnGroup().isPeaceful())
            return false;
        return passenger instanceof LivingEntity;
    }

    public static void sitDown(World world, BlockPos pos, Entity entity) {
        if (world.isClient())
            return;
        SeatEntity seat = new SeatEntity(world, pos);
        seat.setPosition(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        world.spawnEntity(seat);
        entity.startRiding(seat, true);
        if (entity instanceof TameableEntity ta)
            ta.setInSittingPose(true);
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

}
