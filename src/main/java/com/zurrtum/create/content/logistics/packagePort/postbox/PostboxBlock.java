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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class PostboxBlock extends HorizontalDirectionalBlock implements IBE<PostboxBlockEntity>, IWrenchable, ProperWaterloggedBlock, ItemInventoryProvider<PostboxBlockEntity> {
    public static MapCodec<PostboxBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        propertiesCodec(),
        DyeColor.CODEC.fieldOf("color").forGetter(PostboxBlock::getColor)
    ).apply(instance, PostboxBlock::new));

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    protected final DyeColor color;

    public PostboxBlock(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
        registerDefaultState(defaultBlockState().setValue(OPEN, false).setValue(WATERLOGGED, false));
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, PostboxBlockEntity blockEntity, @Nullable Direction context) {
        return blockEntity.inventory;
    }

    public static Function<Properties, PostboxBlock> dyed(DyeColor color) {
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
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction facing = pContext.getHorizontalDirection().getOpposite();
        return withWater(super.getStateForPlacement(pContext).setValue(FACING, facing), pContext);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState updateShape(
        BlockState pState,
        LevelReader pLevel,
        ScheduledTickAccess tickView,
        BlockPos pPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        RandomSource random
    ) {
        updateWater(pLevel, tickView, pState, pPos);
        return pState;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AllShapes.POSTBOX.get(pState.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(FACING, OPEN, WATERLOGGED));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
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
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos, Direction direction) {
        return getBlockEntityOptional(pLevel, pPos).map(PackagePortBlockEntity::getComparatorOutput).orElse(0);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}