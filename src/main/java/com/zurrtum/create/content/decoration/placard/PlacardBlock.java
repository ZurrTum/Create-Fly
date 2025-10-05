package com.zurrtum.create.content.decoration.placard;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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

public class PlacardBlock extends WallMountedBlock implements ProperWaterloggedBlock, IBE<PlacardBlockEntity>, SpecialBlockItemRequirement, IWrenchable {

    public static final BooleanProperty POWERED = Properties.POWERED;

    public static final MapCodec<PlacardBlock> CODEC = createCodec(PlacardBlock::new);

    public PlacardBlock(Settings p_53182_) {
        super(p_53182_);
        setDefaultState(getDefaultState().with(WATERLOGGED, false).with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(FACE, FACING, WATERLOGGED, POWERED));
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        return canAttachLenient(pLevel, pPos, getDirection(pState).getOpposite());
    }

    public static boolean canAttachLenient(WorldView pReader, BlockPos pPos, Direction pDirection) {
        BlockPos blockpos = pPos.offset(pDirection);
        return !pReader.getBlockState(blockpos).getCollisionShape(pReader, blockpos).isEmpty();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState stateForPlacement = super.getPlacementState(pContext);
        if (stateForPlacement == null)
            return null;
        if (stateForPlacement.get(FACE) == BlockFace.FLOOR)
            stateForPlacement = stateForPlacement.with(FACING, stateForPlacement.get(FACING).getOpposite());
        return withWater(stateForPlacement, pContext);
    }

    @Override
    public boolean emitsRedstonePower(BlockState pState) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState pBlockState, BlockView pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.get(POWERED) ? 15 : 0;
    }

    @Override
    public int getStrongRedstonePower(BlockState pBlockState, BlockView pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.get(POWERED) && getDirection(pBlockState) == pSide ? 15 : 0;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.PLACARD.get(getDirection(pState));
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
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
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
        if (player.isSneaking())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient())
            return ActionResult.SUCCESS;

        ItemStack inHand = player.getStackInHand(hand);
        return onBlockEntityUseItemOn(
            level, pos, pte -> {
                ItemStack inBlock = pte.getHeldItem();

                if (!player.canModifyBlocks() || inHand.isEmpty() || !inBlock.isEmpty()) {
                    if (inBlock.isEmpty())
                        return ActionResult.FAIL;
                    if (inHand.isEmpty())
                        return ActionResult.FAIL;
                    if (state.get(POWERED))
                        return ActionResult.FAIL;

                    boolean test = inBlock.getItem() instanceof FilterItem ? FilterItemStack.of(inBlock)
                        .test(level, inHand) : ItemStack.areItemsAndComponentsEqual(inHand, inBlock);
                    if (!test) {
                        AllSoundEvents.DENY.play(level, null, pos, 1, 1);
                        return ActionResult.SUCCESS;
                    }

                    AllSoundEvents.CONFIRM.play(level, null, pos, 1, 1);
                    level.setBlockState(pos, state.with(POWERED, true), Block.NOTIFY_ALL);
                    updateNeighbours(state, level, pos);
                    pte.poweredTicks = 19;
                    pte.notifyUpdate();
                    return ActionResult.SUCCESS;
                }

                level.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1, 1);
                pte.setHeldItem(inHand.copyWithCount(1));

                if (!player.isCreative()) {
                    inHand.decrement(1);
                    if (inHand.isEmpty())
                        player.setStackInHand(hand, ItemStack.EMPTY);
                }

                return ActionResult.SUCCESS;
            }
        );
    }

    public static Direction connectedDirection(BlockState state) {
        return getDirection(state);
    }

    @Override
    public void onStateReplaced(BlockState pState, ServerWorld pLevel, BlockPos pPos, boolean pIsMoving) {
        if (!pIsMoving && pState.get(POWERED))
            updateNeighbours(pState, pLevel, pPos);
    }

    public static void updateNeighbours(BlockState pState, World pLevel, BlockPos pPos) {
        pLevel.updateNeighborsAlways(pPos, pState.getBlock(), null);
        pLevel.updateNeighborsAlways(pPos.offset(getDirection(pState).getOpposite()), pState.getBlock(), null);
    }

    @Override
    public void onBlockBreakStart(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
        if (pLevel.isClient())
            return;
        withBlockEntityDo(
            pLevel, pPos, pte -> {
                ItemStack heldItem = pte.getHeldItem();
                if (heldItem.isEmpty())
                    return;
                pPlayer.getInventory().offerOrDrop(heldItem);
                pLevel.playSound(null, pPos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1, 1);
                pte.setHeldItem(ItemStack.EMPTY);
            }
        );
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        ItemStack placardStack = AllItems.PLACARD.getDefaultStack();
        if (be instanceof PlacardBlockEntity pbe) {
            ItemStack heldItem = pbe.getHeldItem();
            if (!heldItem.isEmpty()) {
                return new ItemRequirement(List.of(
                    new ItemRequirement.StackRequirement(placardStack, ItemUseType.CONSUME),
                    new ItemRequirement.StrictNbtStackRequirement(heldItem, ItemUseType.CONSUME)
                ));
            }
        }
        return new ItemRequirement(ItemUseType.CONSUME, placardStack);
    }

    @Override
    public Class<PlacardBlockEntity> getBlockEntityClass() {
        return PlacardBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PlacardBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.PLACARD;
    }

    @Override
    protected @NotNull MapCodec<? extends WallMountedBlock> getCodec() {
        return CODEC;
    }

}
