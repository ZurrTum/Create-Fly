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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Optional;

public class BasinBlock extends Block implements IBE<BasinBlockEntity>, IWrenchable, ItemInventoryProvider<BasinBlockEntity>, FluidInventoryProvider<BasinBlockEntity> {

    public static final EnumProperty<Direction> FACING = EnumProperty.of("facing", Direction.class, side -> side != Direction.UP);

    public BasinBlock(Settings p_i48440_1_) {
        super(p_i48440_1_);
        setDefaultState(getDefaultState().with(FACING, Direction.DOWN));
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, BasinBlockEntity blockEntity, Direction context) {
        return blockEntity.itemCapability;
    }

    @Override
    public FluidInventory getFluidInventory(WorldAccess world, BlockPos pos, BlockState state, BasinBlockEntity blockEntity, Direction context) {
        return blockEntity.fluidCapability;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> p_206840_1_) {
        super.appendProperties(p_206840_1_.add(FACING));
    }

    public static boolean isBasin(WorldView world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof BasinBlockEntity;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return !(world.getBlockEntity(pos.up()) instanceof BasinOperatingBlockEntity);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        if (!context.getWorld().isClient())
            withBlockEntityDo(context.getWorld(), context.getBlockPos(), bte -> bte.onWrenched(context.getSide()));
        return ActionResult.SUCCESS;
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
        return onBlockEntityUseItemOn(
            level, pos, be -> {
                if (!stack.isEmpty()) {
                    if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, be))
                        return ActionResult.SUCCESS;
                    if (FluidHelper.tryFillItemFromBE(level, player, hand, stack, be))
                        return ActionResult.SUCCESS;

                    if (GenericItemEmptying.canItemBeEmptied(level, stack) || GenericItemFilling.canItemBeFilled(level, stack))
                        return ActionResult.SUCCESS;
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
                                return ActionResult.SUCCESS;
                            }
                        }
                    }
                    return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                }

                Inventory inv = be.itemCapability;
                if (inv == null)
                    inv = new ItemStackHandler(1);
                boolean success = false;
                for (int slot = 0, size = inv.size(); slot < size; slot++) {
                    ItemStack stackInSlot = inv.getStack(slot);
                    if (stackInSlot.isEmpty())
                        continue;
                    player.getInventory().offerOrDrop(stackInSlot);
                    inv.setStack(slot, ItemStack.EMPTY);
                    success = true;
                }
                if (success)
                    level.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .2f, 1f + level.getRandom().nextFloat());
                be.onEmptied();
                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public void onEntityLand(BlockView worldIn, Entity entityIn) {
        super.onEntityLand(worldIn, entityIn);
        if (!worldIn.getBlockState(entityIn.getBlockPos()).isOf(this))
            return;
        if (!(entityIn instanceof ItemEntity itemEntity))
            return;
        if (!entityIn.isAlive())
            return;
        withBlockEntityDo(
            worldIn, entityIn.getBlockPos(), be -> {
                ItemStack stack = itemEntity.getStack();
                int count = stack.getCount();
                int insert = be.itemCapability.insert(stack);
                if (insert == count) {
                    itemEntity.discard();
                } else if (insert != 0) {
                    stack.decrement(insert);
                    itemEntity.setStack(stack);
                }
            }
        );
    }

    @Override
    public VoxelShape getRaycastShape(BlockState p_199600_1_, BlockView p_199600_2_, BlockPos p_199600_3_) {
        return AllShapes.BASIN_RAYTRACE_SHAPE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.BASIN_BLOCK_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView reader, BlockPos pos, ShapeContext ctx) {
        if (ctx instanceof EntityShapeContext entityShapeContext && entityShapeContext.getEntity() instanceof ItemEntity)
            return AllShapes.BASIN_COLLISION_SHAPE;
        return getOutlineShape(state, reader, pos, ctx);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState blockState, World worldIn, BlockPos pos, Direction direction) {
        Optional<BasinBlockEntity> getter = getBlockEntityOptional(worldIn, pos);
        if (getter.isEmpty()) {
            return 0;
        }
        BasinInventory inv = getter.get().itemCapability;
        int i = 0;
        float f = 0.0F;
        for (int j = 0; j < 9; ++j) {
            int slotLimit = inv.getMaxCountPerStack();
            ItemStack itemstack = inv.getStack(j);
            if (!itemstack.isEmpty()) {
                f += (float) itemstack.getCount() / (float) Math.min(slotLimit, itemstack.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 64));
                ++i;
            }
        }
        return MathHelper.floor(f / 9 * 14.0F) + (i > 0 ? 1 : 0);
    }

    @Override
    public Class<BasinBlockEntity> getBlockEntityClass() {
        return BasinBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BasinBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.BASIN;
    }

    public static boolean canOutputTo(BlockView world, BlockPos basinPos, Direction direction) {
        BlockPos neighbour = basinPos.offset(direction);
        BlockPos output = neighbour.down();
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
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

}
