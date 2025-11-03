package com.zurrtum.create.content.kinetics.crusher;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.*;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CrushingWheelControllerBlock extends FacingBlock implements IBE<CrushingWheelControllerBlockEntity>, ItemInventoryProvider<CrushingWheelControllerBlockEntity> {

    public CrushingWheelControllerBlock(Settings p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return 0;
    }

    @Override
    public Inventory getInventory(
        WorldAccess world,
        BlockPos pos,
        BlockState state,
        CrushingWheelControllerBlockEntity blockEntity,
        Direction context
    ) {
        return blockEntity.inventory;
    }

    public static final BooleanProperty VALID = BooleanProperty.of("valid");

    public static final MapCodec<CrushingWheelControllerBlock> CODEC = createCodec(CrushingWheelControllerBlock::new);

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext useContext) {
        return false;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(VALID);
        builder.add(FACING);
        super.appendProperties(builder);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn, EntityCollisionHandler handler, boolean bl) {
        if (!state.get(VALID))
            return;

        Direction facing = state.get(FACING);
        Axis axis = facing.getAxis();

        checkEntityForProcessing(worldIn, pos, entityIn);

        withBlockEntityDo(
            worldIn, pos, be -> {
                if (be.processingEntity == entityIn) {
                    entityIn.slowMovement(
                        state,
                        new Vec3d(
                            axis == Axis.X ? (double) 0.05F : 0.25D,
                            axis == Axis.Y ? (double) 0.05F : 0.25D,
                            axis == Axis.Z ? (double) 0.05F : 0.25D
                        )
                    );
                }
            }
        );
    }

    public void checkEntityForProcessing(World worldIn, BlockPos pos, Entity entityIn) {
        CrushingWheelControllerBlockEntity be = getBlockEntity(worldIn, pos);
        if (be == null)
            return;
        if (be.crushingspeed == 0)
            return;
        //		if (entityIn instanceof ItemEntity)
        //			((ItemEntity) entityIn).setPickUpDelay(10);
        if (entityIn instanceof ItemEntity) {
            Optional<BlockPos> value = AllSynchedDatas.BYPASS_CRUSHING_WHEEL.get(entityIn);
            if (value.isPresent() && pos.equals(value.get()))
                return;
        }
        if (be.isOccupied())
            return;
        if (entityIn instanceof PlayerEntity player) {
            if (player.isCreative())
                return;
            if (entityIn.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL)
                return;
        }

        be.startCrushing(entityIn);
    }

    @Override
    public void onEntityLand(BlockView worldIn, Entity entityIn) {
        super.onEntityLand(worldIn, entityIn);
        // Moved to onEntityCollision to allow for omnidirectional input
    }

    @Override
    public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (!stateIn.get(VALID))
            return;
        if (rand.nextInt(1) != 0)
            return;
        double d0 = (float) pos.getX() + rand.nextFloat();
        double d1 = (float) pos.getY() + rand.nextFloat();
        double d2 = (float) pos.getZ() + rand.nextFloat();
        worldIn.addParticleClient(ParticleTypes.CRIT, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState stateIn,
        WorldView worldIn,
        ScheduledTickView tickView,
        BlockPos currentPos,
        Direction facing,
        BlockPos facingPos,
        BlockState facingState,
        Random random
    ) {
        updateSpeed(stateIn, worldIn, currentPos);
        return stateIn;
    }

    public void updateSpeed(BlockState state, WorldView world, BlockPos pos) {
        withBlockEntityDo(
            world, pos, be -> {
                if (!state.get(VALID)) {
                    if (be.crushingspeed != 0) {
                        be.crushingspeed = 0;
                        be.sendData();
                    }
                    return;
                }

                for (Direction d : Iterate.directions) {
                    BlockState neighbour = world.getBlockState(pos.offset(d));
                    if (!neighbour.isOf(AllBlocks.CRUSHING_WHEEL))
                        continue;
                    if (neighbour.get(Properties.AXIS) == d.getAxis())
                        continue;
                    BlockEntity adjBE = world.getBlockEntity(pos.offset(d));
                    if (!(adjBE instanceof CrushingWheelBlockEntity cwbe))
                        continue;
                    be.crushingspeed = Math.abs(cwbe.getSpeed() / 50f);
                    be.sendData();

                    cwbe.award(AllAdvancements.CRUSHING_WHEEL);
                    if (Math.abs(cwbe.getSpeed()) > AllConfigs.server().kinetics.maxRotationSpeed.get() - 1)
                        cwbe.award(AllAdvancements.CRUSHER_MAXED);

                    break;
                }
            }
        );
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        VoxelShape standardShape = AllShapes.CRUSHING_WHEEL_CONTROLLER_COLLISION.get(state.get(FACING));

        if (!state.get(VALID))
            return standardShape;
        if (!(context instanceof EntityShapeContext entityShapeContext))
            return standardShape;
        Entity entity = entityShapeContext.getEntity();
        if (entity == null)
            return standardShape;

        if (entity instanceof ItemEntity && state.get(FACING) != Direction.UP) {
            Optional<BlockPos> value = AllSynchedDatas.BYPASS_CRUSHING_WHEEL.get(entity);
            if (value.isPresent() && pos.equals(value.get())) // Allow output items to land on top of the block rather
                return VoxelShapes.empty();                    // than falling back through.
        }

        CrushingWheelControllerBlockEntity be = getBlockEntity(worldIn, pos);
        if (be != null && be.processingEntity == entity)
            return VoxelShapes.empty();

        return standardShape;
    }

    @Override
    public Class<CrushingWheelControllerBlockEntity> getBlockEntityClass() {
        return CrushingWheelControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CrushingWheelControllerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    protected @NotNull MapCodec<? extends FacingBlock> getCodec() {
        return CODEC;
    }
}
