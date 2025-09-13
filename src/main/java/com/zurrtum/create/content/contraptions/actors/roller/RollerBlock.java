package com.zurrtum.create.content.contraptions.actors.roller;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.content.contraptions.actors.AttachedActorBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.placement.PoleHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class RollerBlock extends AttachedActorBlock implements IBE<RollerBlockEntity> {
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public static final MapCodec<RollerBlock> CODEC = createCodec(RollerBlock::new);

    public RollerBlock(Settings p_i48377_1_) {
        super(p_i48377_1_);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return withWater(getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite()), context);
    }

    @Override
    public Class<RollerBlockEntity> getBlockEntityClass() {
        return RollerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RollerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.MECHANICAL_ROLLER;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        withBlockEntityDo(pLevel, pPos, RollerBlockEntity::searchForSharedValues);
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
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isSneaking() && player.canModifyBlocks()) {
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    private static class PlacementHelper extends PoleHelper<Direction> {

        public PlacementHelper() {
            super(state -> state.isOf(AllBlocks.MECHANICAL_ROLLER), state -> state.get(FACING).rotateYClockwise().getAxis(), FACING);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.isOf(AllItems.MECHANICAL_ROLLER);
        }

    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

}
