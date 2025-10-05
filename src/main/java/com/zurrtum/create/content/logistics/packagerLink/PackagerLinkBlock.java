package com.zurrtum.create.content.logistics.packagerLink;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class PackagerLinkBlock extends WallMountedBlock implements IBE<PackagerLinkBlockEntity>, ProperWaterloggedBlock, IWrenchable {
    public static final MapCodec<PackagerLinkBlock> CODEC = createCodec(PackagerLinkBlock::new);

    public static final BooleanProperty POWERED = Properties.POWERED;

    public PackagerLinkBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POWERED, false).with(WATERLOGGED, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockPos pos = context.getBlockPos();
        BlockState placed = super.getPlacementState(context);
        if (placed == null)
            return null;
        if (placed.get(FACE) == BlockFace.CEILING)
            placed = placed.with(FACING, placed.get(FACING).getOpposite());
        return withWater(placed.with(POWERED, getPower(placed, context.getWorld(), pos) > 0), context);
    }

    public static Direction getConnectedDirection(BlockState state) {
        return WallMountedBlock.getDirection(state);
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        return true;
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
    public void neighborUpdate(
        BlockState state,
        World worldIn,
        BlockPos pos,
        Block blockIn,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        if (worldIn.isClient())
            return;

        int power = getPower(state, worldIn, pos);
        boolean powered = power > 0;
        boolean previouslyPowered = state.get(POWERED);
        if (previouslyPowered != powered)
            worldIn.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
        withBlockEntityDo(worldIn, pos, link -> link.behaviour.redstonePowerChanged(power));
    }

    public static int getPower(BlockState state, World worldIn, BlockPos pos) {
        int power = 0;
        for (Direction d : Iterate.directions)
            if (d.getOpposite() != getConnectedDirection(state))
                power = Math.max(power, worldIn.getEmittedRedstonePower(pos.offset(d), d));
        return power;
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        withBlockEntityDo(
            pLevel, pPos, plbe -> {
                if (pPlacer instanceof PlayerEntity player) {
                    plbe.placedBy = player.getUuid();
                    plbe.notifyUpdate();
                }
            }
        );
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.STOCK_LINK.get(getConnectedDirection(pState));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED, WATERLOGGED, FACE, FACING));
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public Class<PackagerLinkBlockEntity> getBlockEntityClass() {
        return PackagerLinkBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PackagerLinkBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.PACKAGER_LINK;
    }

    @Override
    protected MapCodec<? extends WallMountedBlock> getCodec() {
        return CODEC;
    }
}
