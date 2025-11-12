package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BasinBlock extends Block implements IBE<BasinBlockEntity>, IWrenchable, ItemInventoryProvider<BasinBlockEntity>, FluidInventoryProvider<BasinBlockEntity> {

    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, side -> side != Direction.UP);

    public BasinBlock(Properties p_i48440_1_) {
        super(p_i48440_1_);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, BasinBlockEntity blockEntity, Direction context) {
        return blockEntity.itemCapability;
    }

    @Override
    public FluidInventory getFluidInventory(LevelAccessor world, BlockPos pos, BlockState state, BasinBlockEntity blockEntity, Direction context) {
        return blockEntity.fluidCapability;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> p_206840_1_) {
        super.createBlockStateDefinition(p_206840_1_.add(FACING));
    }

    public static boolean isBasin(LevelReader world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof BasinBlockEntity;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return !(world.getBlockEntity(pos.above()) instanceof BasinOperatingBlockEntity);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (!context.getLevel().isClientSide())
            withBlockEntityDo(context.getLevel(), context.getClickedPos(), bte -> bte.onWrenched(context.getClickedFace()));
        return InteractionResult.SUCCESS;
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
        return onBlockEntityUseItemOn(
            level, pos, be -> {
                if (!stack.isEmpty()) {
                    if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, be))
                        return InteractionResult.SUCCESS;
                    if (FluidHelper.tryFillItemFromBE(level, player, hand, stack, be))
                        return InteractionResult.SUCCESS;

                    if (GenericItemEmptying.canItemBeEmptied(level, stack) || GenericItemFilling.canItemBeFilled(level, stack))
                        return InteractionResult.SUCCESS;
                    if (stack.getItem().equals(Items.SPONGE)) {
                        FluidInventory fluidHandler = be.fluidCapability;
                        if (fluidHandler != null) {
                            boolean drained = false;
                            for (int i = 0, size = fluidHandler.size(); i < size; i++) {
                                if (fluidHandler.getStack(i).isEmpty()) {
                                    continue;
                                }
                                fluidHandler.setStack(i, FluidStack.EMPTY);
                                drained = true;
                            }
                            if (drained) {
                                fluidHandler.markDirty();
                                return InteractionResult.SUCCESS;
                            }
                        }
                    }
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                }

                Container inv = be.itemCapability;
                if (inv == null)
                    inv = new ItemStackHandler(1);
                boolean success = false;
                for (int slot = 0, size = inv.getContainerSize(); slot < size; slot++) {
                    ItemStack stackInSlot = inv.getItem(slot);
                    if (stackInSlot.isEmpty())
                        continue;
                    player.getInventory().placeItemBackInInventory(stackInSlot);
                    inv.setItem(slot, ItemStack.EMPTY);
                    success = true;
                }
                if (success)
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f, 1f + level.getRandom().nextFloat());
                be.onEmptied();
                return InteractionResult.SUCCESS;
            }
        );
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter worldIn, Entity entityIn) {
        super.updateEntityMovementAfterFallOn(worldIn, entityIn);
        if (!worldIn.getBlockState(entityIn.blockPosition()).is(this))
            return;
        if (!(entityIn instanceof ItemEntity itemEntity))
            return;
        if (!entityIn.isAlive())
            return;
        withBlockEntityDo(
            worldIn, entityIn.blockPosition(), be -> {
                ItemStack stack = itemEntity.getItem();
                int count = stack.getCount();
                int insert = be.itemCapability.insert(stack);
                if (insert == count) {
                    itemEntity.discard();
                } else if (insert != 0) {
                    stack.shrink(insert);
                    itemEntity.setItem(stack);
                }
            }
        );
    }

    @Override
    public VoxelShape getInteractionShape(BlockState p_199600_1_, BlockGetter p_199600_2_, BlockPos p_199600_3_) {
        return AllShapes.BASIN_RAYTRACE_SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.BASIN_BLOCK_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext entityShapeContext && entityShapeContext.getEntity() instanceof ItemEntity)
            return AllShapes.BASIN_COLLISION_SHAPE;
        return getShape(state, reader, pos, ctx);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos, Direction direction) {
        Optional<BasinBlockEntity> getter = getBlockEntityOptional(worldIn, pos);
        if (getter.isEmpty()) {
            return 0;
        }
        BasinInventory inv = getter.get().itemCapability;
        int i = 0;
        float f = 0.0F;
        for (int j = 0; j < 9; ++j) {
            int slotLimit = inv.getMaxStackSize();
            ItemStack itemstack = inv.getItem(j);
            if (!itemstack.isEmpty()) {
                f += (float) itemstack.getCount() / (float) Math.min(slotLimit, itemstack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64));
                ++i;
            }
        }
        return Mth.floor(f / 9 * 14.0F) + (i > 0 ? 1 : 0);
    }

    @Override
    public Class<BasinBlockEntity> getBlockEntityClass() {
        return BasinBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BasinBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.BASIN;
    }

    public static boolean canOutputTo(BlockGetter world, BlockPos basinPos, Direction direction) {
        BlockPos neighbour = basinPos.relative(direction);
        BlockPos output = neighbour.below();
        BlockState blockState = world.getBlockState(neighbour);

        if (FunnelBlock.isFunnel(blockState)) {
            if (FunnelBlock.getFunnelFacing(blockState) == direction)
                return false;
        } else if (!blockState.getCollisionShape(world, neighbour).isEmpty()) {
            return false;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(output);
            if (blockEntity instanceof BeltBlockEntity belt) {
                return belt.getSpeed() == 0 || belt.getMovementFacing() != direction.getOpposite();
            }
        }

        DirectBeltInputBehaviour directBeltInputBehaviour = BlockEntityBehaviour.get(world, output, DirectBeltInputBehaviour.TYPE);
        if (directBeltInputBehaviour != null)
            return directBeltInputBehaviour.canInsertFromSide(direction);
        return false;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

}
