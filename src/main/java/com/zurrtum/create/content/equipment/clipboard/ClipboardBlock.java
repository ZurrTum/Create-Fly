package com.zurrtum.create.content.equipment.clipboard;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClipboardBlock extends WallMountedBlock implements IBE<ClipboardBlockEntity>, IWrenchable, ProperWaterloggedBlock {

    public static final BooleanProperty WRITTEN = BooleanProperty.of("written");

    public static final MapCodec<ClipboardBlock> CODEC = createCodec(ClipboardBlock::new);

    public ClipboardBlock(Settings pProperties) {
        super(pProperties);
        setDefaultState(getDefaultState().with(WATERLOGGED, false).with(WRITTEN, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(WRITTEN, FACE, FACING, WATERLOGGED));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState stateForPlacement = super.getPlacementState(pContext);
        if (stateForPlacement == null)
            return null;
        if (stateForPlacement.get(FACE) != BlockFace.WALL)
            stateForPlacement = stateForPlacement.with(FACING, stateForPlacement.get(FACING).getOpposite());
        return withWater(stateForPlacement, pContext).with(WRITTEN, !pContext.getStack().getComponentChanges().isEmpty());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return (switch (pState.get(FACE)) {
            case FLOOR -> AllShapes.CLIPBOARD_FLOOR;
            case CEILING -> AllShapes.CLIPBOARD_CEILING;
            default -> AllShapes.CLIPBOARD_WALL;
        }).get(pState.get(FACING));
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        return !pLevel.getBlockState(pPos.offset(getDirection(pState).getOpposite())).isReplaceable();
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        if (player.isSneaking()) {
            breakAndCollect(state, level, pos, player);
            return ActionResult.SUCCESS;
        }

        return onBlockEntityUse(
            level, pos, cbe -> {
                if (level.isClient())
                    AllClientHandle.INSTANCE.openClipboardScreen(player, cbe.getComponents(), pos);
                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public void onBlockBreakStart(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
        breakAndCollect(pState, pLevel, pPos, pPlayer);
    }

    private void breakAndCollect(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
        if (FakePlayerHandler.has(pPlayer))
            return;
        if (pLevel.isClient)
            return;
        ItemStack cloneItemStack = getPickStack(pLevel, pPos, pState, true);
        pLevel.breakBlock(pPos, false);
        if (pLevel.getBlockState(pPos) != pState) {
            PlayerInventory inv = pPlayer.getInventory();
            ItemStack selected = inv.getSelectedStack();
            if (selected.isEmpty()) {
                inv.setSelectedStack(cloneItemStack);
            } else {
                inv.offerOrDrop(cloneItemStack);
            }
        }
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return applyComponentsToDropStack(new ItemStack(this), world.getBlockEntity(pos));
    }

    @Override
    public BlockState onBreak(World level, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!(level.getBlockEntity(pos) instanceof ClipboardBlockEntity cbe))
            return state;
        if (level.isClient || player.isCreative())
            return state;
        Block.dropStack(level, pos, applyComponentsToDropStack(new ItemStack(this), cbe));

        return state;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState pState, LootWorldContext.Builder pBuilder) {
        if (!(pBuilder.getOptional(LootContextParameters.BLOCK_ENTITY) instanceof ClipboardBlockEntity cbe))
            return super.getDroppedStacks(pState, pBuilder);
        ItemStack drop = applyComponentsToDropStack(new ItemStack(this), cbe);
        pBuilder.addDynamicDrop(ShulkerBoxBlock.CONTENTS_DYNAMIC_DROP_ID, c -> c.accept(drop.copy()));
        return ImmutableList.of(drop);
    }

    private ItemStack applyComponentsToDropStack(ItemStack stack, BlockEntity blockEntity) {
        if (blockEntity instanceof ClipboardBlockEntity cbe) {
            stack.applyComponentsFrom(cbe.getComponents());
            return stack;
        }
        return stack;
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
        BlockPos pCurrentPos,
        Direction pFacing,
        BlockPos pFacingPos,
        BlockState pFacingState,
        Random random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return super.getStateForNeighborUpdate(pState, pLevel, tickView, pCurrentPos, pFacing, pFacingPos, pFacingState, random);
    }

    @Override
    public Class<ClipboardBlockEntity> getBlockEntityClass() {
        return ClipboardBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ClipboardBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CLIPBOARD;
    }

    @Override
    protected @NotNull MapCodec<? extends WallMountedBlock> getCodec() {
        return CODEC;
    }
}
