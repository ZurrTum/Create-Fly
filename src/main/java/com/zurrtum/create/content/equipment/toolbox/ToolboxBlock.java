package com.zurrtum.create.content.equipment.toolbox;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.*;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

import static net.minecraft.state.property.Properties.WATERLOGGED;

public class ToolboxBlock extends HorizontalFacingBlock implements Waterloggable, IBE<ToolboxBlockEntity>, ItemInventoryProvider<ToolboxBlockEntity> {

    protected final DyeColor color;

    public static final MapCodec<ToolboxBlock> CODEC = createCodec(p -> new ToolboxBlock(p, DyeColor.WHITE));

    public ToolboxBlock(Settings properties, DyeColor color) {
        super(properties);
        this.color = color;
        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, ToolboxBlockEntity blockEntity, Direction context) {
        return blockEntity.inventory;
    }

    public static Function<Settings, ToolboxBlock> dyed(DyeColor color) {
        return settings -> new ToolboxBlock(settings, color);
    }

    public static ToolboxBlock getColorBlock(DyeColor color) {
        return switch (color) {
            case WHITE -> AllBlocks.WHITE_TOOLBOX;
            case ORANGE -> AllBlocks.ORANGE_TOOLBOX;
            case MAGENTA -> AllBlocks.MAGENTA_TOOLBOX;
            case LIGHT_BLUE -> AllBlocks.LIGHT_BLUE_TOOLBOX;
            case YELLOW -> AllBlocks.YELLOW_TOOLBOX;
            case LIME -> AllBlocks.LIME_TOOLBOX;
            case PINK -> AllBlocks.PINK_TOOLBOX;
            case GRAY -> AllBlocks.GRAY_TOOLBOX;
            case LIGHT_GRAY -> AllBlocks.LIGHT_GRAY_TOOLBOX;
            case CYAN -> AllBlocks.CYAN_TOOLBOX;
            case PURPLE -> AllBlocks.PURPLE_TOOLBOX;
            case BLUE -> AllBlocks.BLUE_TOOLBOX;
            case BROWN -> AllBlocks.BROWN_TOOLBOX;
            case GREEN -> AllBlocks.GREEN_TOOLBOX;
            case RED -> AllBlocks.RED_TOOLBOX;
            case BLACK -> AllBlocks.BLACK_TOOLBOX;
        };
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(WATERLOGGED).add(FACING));
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(worldIn, pos, state, placer, stack);
        if (worldIn.isClient())
            return;
        if (stack == null)
            return;
        withBlockEntityDo(
            worldIn, pos, be -> {
                be.readInventory(stack.get(AllDataComponents.TOOLBOX_INVENTORY));
                if (stack.contains(AllDataComponents.TOOLBOX_UUID))
                    be.setUniqueId(stack.get(AllDataComponents.TOOLBOX_UUID));
                if (stack.contains(DataComponentTypes.CUSTOM_NAME))
                    be.setCustomName(stack.getName());
            }
        );
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (FakePlayerHandler.has(player))
            return;
        if (world.isClient())
            return;
        withBlockEntityDo(world, pos, ToolboxBlockEntity::unequipTracked);
        if (world instanceof ServerWorld) {
            ItemStack cloneItemStack = getPickStack(world, pos, state, true);
            withBlockEntityDo(
                world, pos, i -> {
                    cloneItemStack.applyComponentsFrom(i.createComponentMap());
                }
            );
            world.breakBlock(pos, false);
            if (world.getBlockState(pos) != state)
                player.getInventory().offerOrDrop(cloneItemStack);
        }
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack item = new ItemStack(this);
        Optional<ToolboxBlockEntity> blockEntityOptional = getBlockEntityOptional(world, pos);

        blockEntityOptional.map(tb -> item.set(AllDataComponents.TOOLBOX_INVENTORY, tb.inventory));

        blockEntityOptional.map(ToolboxBlockEntity::getUniqueId).ifPresent(uid -> item.set(AllDataComponents.TOOLBOX_UUID, uid));
        blockEntityOptional.map(ToolboxBlockEntity::getCustomName).ifPresent(name -> item.set(DataComponentTypes.CUSTOM_NAME, name));
        return item;
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighbourPos,
        BlockState neighbourState,
        Random random
    ) {
        if (state.get(WATERLOGGED))
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        return state;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return AllShapes.TOOLBOX.get(state.get(FACING));
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
        if (player == null || player.isInSneakingPose())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        DyeColor color = AllItemTags.getDyeColor(stack);
        if (color != null && color != this.color) {
            if (level.isClient())
                return ActionResult.SUCCESS;
            BlockState newState = BlockHelper.copyProperties(state, getColorBlock(color).getDefaultState());
            level.setBlockState(pos, newState);
            return ActionResult.SUCCESS;
        }

        if (FakePlayerHandler.has(player))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient())
            return ActionResult.SUCCESS;

        withBlockEntityDo(level, pos, toolbox -> toolbox.openHandledScreen((ServerPlayerEntity) player));
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState ifluidstate = context.getWorld().getFluidState(context.getBlockPos());
        return super.getPlacementState(context).with(FACING, context.getHorizontalPlayerFacing().getOpposite())
            .with(WATERLOGGED, Boolean.valueOf(ifluidstate.getFluid() == Fluids.WATER));
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
    public boolean hasComparatorOutput(BlockState pState) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState pState, World pLevel, BlockPos pPos) {
        return ItemHelper.calcRedstoneFromBlockEntity(this, pLevel, pPos);
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}
