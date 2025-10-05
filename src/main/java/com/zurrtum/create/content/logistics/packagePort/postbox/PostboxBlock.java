package com.zurrtum.create.content.logistics.packagePort.postbox;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
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

import java.util.function.Function;

public class PostboxBlock extends HorizontalFacingBlock implements IBE<PostboxBlockEntity>, IWrenchable, ProperWaterloggedBlock, ItemInventoryProvider<PostboxBlockEntity> {
    public static MapCodec<PostboxBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        createSettingsCodec(),
        DyeColor.CODEC.fieldOf("color").forGetter(PostboxBlock::getColor)
    ).apply(instance, PostboxBlock::new));

    public static final BooleanProperty OPEN = Properties.OPEN;

    protected final DyeColor color;

    public PostboxBlock(Settings properties, DyeColor color) {
        super(properties);
        this.color = color;
        setDefaultState(getDefaultState().with(OPEN, false).with(WATERLOGGED, false));
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, PostboxBlockEntity blockEntity, Direction context) {
        return blockEntity.inventory;
    }

    public static Function<Settings, PostboxBlock> dyed(DyeColor color) {
        return settings -> new PostboxBlock(settings, color);
    }

    public static PostboxBlock getColorBlock(DyeColor color) {
        return switch (color) {
            case WHITE -> AllBlocks.WHITE_POSTBOX;
            case ORANGE -> AllBlocks.ORANGE_POSTBOX;
            case MAGENTA -> AllBlocks.MAGENTA_POSTBOX;
            case LIGHT_BLUE -> AllBlocks.LIGHT_BLUE_POSTBOX;
            case YELLOW -> AllBlocks.YELLOW_POSTBOX;
            case LIME -> AllBlocks.LIME_POSTBOX;
            case PINK -> AllBlocks.PINK_POSTBOX;
            case GRAY -> AllBlocks.GRAY_POSTBOX;
            case LIGHT_GRAY -> AllBlocks.LIGHT_GRAY_POSTBOX;
            case CYAN -> AllBlocks.CYAN_POSTBOX;
            case PURPLE -> AllBlocks.PURPLE_POSTBOX;
            case BLUE -> AllBlocks.BLUE_POSTBOX;
            case BROWN -> AllBlocks.BROWN_POSTBOX;
            case GREEN -> AllBlocks.GREEN_POSTBOX;
            case RED -> AllBlocks.RED_POSTBOX;
            case BLACK -> AllBlocks.BLACK_POSTBOX;
        };
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        Direction facing = pContext.getHorizontalPlayerFacing().getOpposite();
        return withWater(super.getPlacementState(pContext).with(FACING, facing), pContext);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        updateWater(pLevel, tickView, pState, pPos);
        return pState;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.POSTBOX.get(pState.get(FACING));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(FACING, OPEN, WATERLOGGED));
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        return onBlockEntityUse(level, pos, be -> be.use(player));
    }

    @Override
    public Class<PostboxBlockEntity> getBlockEntityClass() {
        return PostboxBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PostboxBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.PACKAGE_POSTBOX;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public boolean hasComparatorOutput(BlockState pState) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState pState, World pLevel, BlockPos pPos, Direction direction) {
        return getBlockEntityOptional(pLevel, pPos).map(PackagePortBlockEntity::getComparatorOutput).orElse(0);
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}