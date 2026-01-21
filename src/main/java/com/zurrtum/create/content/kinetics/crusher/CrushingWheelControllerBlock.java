package com.zurrtum.create.content.kinetics.crusher;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.*;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class CrushingWheelControllerBlock extends DirectionalBlock implements IBE<CrushingWheelControllerBlockEntity>, ItemInventoryProvider<CrushingWheelControllerBlockEntity> {

    public CrushingWheelControllerBlock(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        return 0;
    }

    @Override
    public Container getInventory(
        LevelAccessor world,
        BlockPos pos,
        BlockState state,
        CrushingWheelControllerBlockEntity blockEntity,
        @Nullable Direction context
    ) {
        return blockEntity.inventory;
    }

    public static final BooleanProperty VALID = BooleanProperty.create("valid");

    public static final MapCodec<CrushingWheelControllerBlock> CODEC = simpleCodec(CrushingWheelControllerBlock::new);

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(VALID);
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn, InsideBlockEffectApplier handler, boolean bl) {
        if (!state.getValue(VALID))
            return;

        Direction facing = state.getValue(FACING);
        Axis axis = facing.getAxis();

        checkEntityForProcessing(worldIn, pos, entityIn);

        withBlockEntityDo(
            worldIn, pos, be -> {
                if (be.processingEntity == entityIn) {
                    entityIn.makeStuckInBlock(
                        state,
                        new Vec3(
                            axis == Axis.X ? (double) 0.05F : 0.25D,
                            axis == Axis.Y ? (double) 0.05F : 0.25D,
                            axis == Axis.Z ? (double) 0.05F : 0.25D
                        )
                    );
                }
            }
        );
    }

    public void checkEntityForProcessing(Level worldIn, BlockPos pos, Entity entityIn) {
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
        if (entityIn instanceof Player player) {
            if (player.isCreative())
                return;
            if (entityIn.level().getDifficulty() == Difficulty.PEACEFUL)
                return;
        }

        be.startCrushing(entityIn);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter worldIn, Entity entityIn) {
        super.updateEntityMovementAfterFallOn(worldIn, entityIn);
        // Moved to onEntityCollision to allow for omnidirectional input
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (!stateIn.getValue(VALID))
            return;
        if (rand.nextInt(1) != 0)
            return;
        double d0 = (float) pos.getX() + rand.nextFloat();
        double d1 = (float) pos.getY() + rand.nextFloat();
        double d2 = (float) pos.getZ() + rand.nextFloat();
        worldIn.addParticle(ParticleTypes.CRIT, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public BlockState updateShape(
        BlockState stateIn,
        LevelReader worldIn,
        ScheduledTickAccess tickView,
        BlockPos currentPos,
        Direction facing,
        BlockPos facingPos,
        BlockState facingState,
        RandomSource random
    ) {
        updateSpeed(stateIn, worldIn, currentPos);
        return stateIn;
    }

    public void updateSpeed(BlockState state, LevelReader world, BlockPos pos) {
        withBlockEntityDo(
            world, pos, be -> {
                if (!state.getValue(VALID)) {
                    if (be.crushingspeed != 0) {
                        be.crushingspeed = 0;
                        be.sendData();
                    }
                    return;
                }

                for (Direction d : Iterate.directions) {
                    BlockState neighbour = world.getBlockState(pos.relative(d));
                    if (!neighbour.is(AllBlocks.CRUSHING_WHEEL))
                        continue;
                    if (neighbour.getValue(BlockStateProperties.AXIS) == d.getAxis())
                        continue;
                    BlockEntity adjBE = world.getBlockEntity(pos.relative(d));
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
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        VoxelShape standardShape = AllShapes.CRUSHING_WHEEL_CONTROLLER_COLLISION.get(state.getValue(FACING));

        if (!state.getValue(VALID))
            return standardShape;
        if (!(context instanceof EntityCollisionContext entityShapeContext))
            return standardShape;
        Entity entity = entityShapeContext.getEntity();
        if (entity == null)
            return standardShape;

        if (entity instanceof ItemEntity && state.getValue(FACING) != Direction.UP) {
            Optional<BlockPos> value = AllSynchedDatas.BYPASS_CRUSHING_WHEEL.get(entity);
            if (value.isPresent() && pos.equals(value.get())) // Allow output items to land on top of the block rather
                return Shapes.empty();                    // than falling back through.
        }

        CrushingWheelControllerBlockEntity be = getBlockEntity(worldIn, pos);
        if (be != null && be.processingEntity == entity)
            return Shapes.empty();

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
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }
}
