package com.zurrtum.create.content.equipment.toolbox;

import com.zurrtum.create.*;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class ToolboxBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock, IBE<ToolboxBlockEntity>, ItemInventoryProvider<ToolboxBlockEntity> {

    protected final DyeColor color;

    public ToolboxBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected boolean shouldChangedStateKeepBlockEntity(BlockState blockState) {
        return AllBlockEntityTypes.TOOLBOX.isValid(blockState);
    }

    @Override
    public Container getInventory(
        LevelAccessor world,
        BlockPos pos,
        BlockState state,
        ToolboxBlockEntity blockEntity,
        @Nullable Direction context
    ) {
        return blockEntity.inventory;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(WATERLOGGED).add(FACING));
    }

    @Override
    public void setPlacedBy(
        Level worldIn,
        BlockPos pos,
        BlockState state,
        @Nullable LivingEntity placer,
        ItemStack stack
    ) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if (worldIn.isClientSide()) {
            return;
        }
        if (stack == null) {
            return;
        }
        withBlockEntityDo(
            worldIn, pos, be -> {
                be.readInventory(stack.get(AllDataComponents.TOOLBOX_INVENTORY));
                if (stack.has(AllDataComponents.TOOLBOX_UUID)) {
                    be.setUniqueId(stack.get(AllDataComponents.TOOLBOX_UUID));
                }
                if (stack.has(DataComponents.CUSTOM_NAME)) {
                    be.setCustomName(stack.getHoverName());
                }
            }
        );
    }

    @Override
    public void attack(BlockState state, Level world, BlockPos pos, Player player) {
        if (FakePlayerHandler.has(player)) {
            return;
        }
        if (world.isClientSide()) {
            return;
        }
        withBlockEntityDo(world, pos, ToolboxBlockEntity::unequipTracked);
        if (world instanceof ServerLevel) {
            ItemStack cloneItemStack = getCloneItemStack(world, pos, state, true);
            withBlockEntityDo(
                world, pos, i -> {
                    cloneItemStack.applyComponents(i.collectComponents());
                }
            );
            world.destroyBlock(pos, false);
            if (world.getBlockState(pos) != state) {
                player.getInventory().placeItemBackInInventory(cloneItemStack, Prediction.SERVER_ONLY);
            }
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack item = new ItemStack(this);
        Optional<ToolboxBlockEntity> blockEntityOptional = getBlockEntityOptional(world, pos);

        blockEntityOptional.map(tb -> item.set(AllDataComponents.TOOLBOX_INVENTORY, tb.inventory));

        blockEntityOptional.map(ToolboxBlockEntity::getUniqueId)
            .ifPresent(uid -> item.set(AllDataComponents.TOOLBOX_UUID, uid));
        blockEntityOptional.map(ToolboxBlockEntity::getCustomName)
            .ifPresent(name -> item.set(DataComponents.CUSTOM_NAME, name));
        return item;
    }

    @Override
    public BlockState updateShape(
        BlockState state,
        LevelReader world,
        ScheduledTickAccess tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighbourPos,
        BlockState neighbourState,
        RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return AllShapes.TOOLBOX.get(state.getValue(FACING));
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
        if (player == null || player.isCrouching()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        DyeColor color = AllItemTags.getDyeColor(stack);
        if (color != null && color != this.color) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            BlockState newState = BlockHelper.copyProperties(state, AllBlocks.TOOLBOX.pick(color).defaultBlockState());
            level.setBlockAndUpdate(pos, newState);
            return InteractionResult.SUCCESS;
        }

        if (FakePlayerHandler.has(player)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        withBlockEntityDo(level, pos, toolbox -> toolbox.openHandledScreen((ServerPlayer) player));
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(WATERLOGGED, ifluidstate.getType() == Fluids.WATER);
    }

    @Override
    public Class<ToolboxBlockEntity> getBlockEntityClass() {
        return ToolboxBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ToolboxBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TOOLBOX;
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos, Direction direction) {
        return ItemHelper.calcRedstoneFromBlockEntity(this, pLevel, pPos);
    }
}
