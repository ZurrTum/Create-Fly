package com.zurrtum.create.content.redstone.displayLink;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.redstone.displayLink.source.RedstonePowerDisplaySource;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DisplayLinkBlock extends WrenchableDirectionalBlock implements IBE<DisplayLinkBlockEntity>, NeighborUpdateListeningBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;

    public static final MapCodec<DisplayLinkBlock> CODEC = createCodec(DisplayLinkBlock::new);

    public DisplayLinkBlock(Settings p_i48415_1_) {
        super(p_i48415_1_);
        setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState placed = super.getPlacementState(context);
        placed = placed.with(FACING, context.getSide());
        return placed.with(POWERED, shouldBePowered(placed, context.getWorld(), context.getBlockPos()));
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    public static void notifyGatherers(WorldAccess level, BlockPos pos) {
        forEachAttachedGatherer(level, pos, DisplayLinkBlockEntity::tickSource);
    }

    @SuppressWarnings("unchecked")
    public static <T extends DisplaySource> void sendToGatherers(
        WorldAccess level,
        BlockPos pos,
        BiConsumer<DisplayLinkBlockEntity, T> callback,
        Class<T> type
    ) {
        forEachAttachedGatherer(
            level, pos, dgte -> {
                if (type.isInstance(dgte.activeSource))
                    callback.accept(dgte, (T) dgte.activeSource);
            }
        );
    }

    private static void forEachAttachedGatherer(WorldAccess level, BlockPos pos, Consumer<DisplayLinkBlockEntity> callback) {
        for (Direction d : Iterate.directions) {
            BlockPos offsetPos = pos.offset(d);
            BlockState blockState = level.getBlockState(offsetPos);
            if (!blockState.isOf(AllBlocks.DISPLAY_LINK))
                continue;

            BlockEntity blockEntity = level.getBlockEntity(offsetPos);
            if (!(blockEntity instanceof DisplayLinkBlockEntity dlbe))
                continue;
            if (dlbe.activeSource == null)
                continue;
            if (dlbe.getDirection() != d.getOpposite())
                continue;

            callback.accept(dlbe);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (worldIn.isClient)
            return;
        if (fromPos.equals(pos.offset(state.get(FACING).getOpposite())))
            sendToGatherers(worldIn, fromPos, (dlte, p) -> dlte.tickSource(), RedstonePowerDisplaySource.class);
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World worldIn,
        BlockPos pos,
        Block blockIn,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        if (worldIn.isClient)
            return;

        boolean powered = shouldBePowered(state, worldIn, pos);
        boolean previouslyPowered = state.get(POWERED);
        if (previouslyPowered != powered) {
            worldIn.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
            if (!powered)
                withBlockEntityDo(worldIn, pos, DisplayLinkBlockEntity::onNoLongerPowered);
        }
    }

    private boolean shouldBePowered(BlockState state, World worldIn, BlockPos pos) {
        boolean powered = false;
        for (Direction d : Iterate.directions) {
            if (d.getOpposite() == state.get(FACING))
                continue;
            if (worldIn.getEmittedRedstonePower(pos.offset(d), d) == 0)
                continue;
            powered = true;
            break;
        }
        return powered;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED));
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        if (player == null)
            return ActionResult.PASS;
        if (player.isSneaking())
            return ActionResult.PASS;
        if (level.isClient) {
            withBlockEntityDo(level, pos, be -> AllClientHandle.INSTANCE.openDisplayLinkScreen(be, player));
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.DATA_GATHERER.get(pState.get(FACING));
    }

    @Override
    public Class<DisplayLinkBlockEntity> getBlockEntityClass() {
        return DisplayLinkBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DisplayLinkBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.DISPLAY_LINK;
    }

    @Override
    protected @NotNull MapCodec<? extends FacingBlock> getCodec() {
        return CODEC;
    }
}
