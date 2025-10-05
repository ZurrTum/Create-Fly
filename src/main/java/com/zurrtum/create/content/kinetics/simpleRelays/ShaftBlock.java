package com.zurrtum.create.content.kinetics.simpleRelays;

import com.google.common.base.Predicates;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.decoration.encasing.EncasableBlock;
import com.zurrtum.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.zurrtum.create.foundation.placement.PoleHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class ShaftBlock extends AbstractSimpleShaftBlock implements EncasableBlock {

    public static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public ShaftBlock(Settings properties) {
        super(properties);
    }

    public static boolean isShaft(BlockState state) {
        return state.isOf(AllBlocks.SHAFT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState stateForPlacement = super.getPlacementState(context);
        return pickCorrectShaftType(stateForPlacement, context.getWorld(), context.getBlockPos());
    }

    public static BlockState pickCorrectShaftType(BlockState stateForPlacement, World level, BlockPos pos) {
        if (PoweredShaftBlock.stillValid(stateForPlacement, level, pos))
            return PoweredShaftBlock.getEquivalent(stateForPlacement);
        return stateForPlacement;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.SIX_VOXEL_POLE.get(state.get(AXIS));
    }

    @Override
    public float getParticleTargetRadius() {
        return .35f;
    }

    @Override
    public float getParticleInitialRadius() {
        return .125f;
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
        if (player.isSneaking() || !player.canModifyBlocks())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        ActionResult result = tryEncase(state, level, pos, stack, player, hand, hitResult);
        if (result.isAccepted())
            return result;

        if (stack.isOf(AllItems.METAL_GIRDER) && state.get(AXIS) != Axis.Y) {
            KineticBlockEntity.switchToBlockState(
                level,
                pos,
                AllBlocks.METAL_GIRDER_ENCASED_SHAFT.getDefaultState().with(WATERLOGGED, state.get(WATERLOGGED))
                    .with(GirderEncasedShaftBlock.HORIZONTAL_AXIS, state.get(AXIS) == Axis.Z ? Axis.Z : Axis.X)
            );
            if (!level.isClient() && !player.isCreative()) {
                stack.decrement(1);
                if (stack.isEmpty())
                    player.setStackInHand(hand, ItemStack.EMPTY);
            }
            return ActionResult.SUCCESS;
        }

        IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(stack))
            return helper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    private static class PlacementHelper extends PoleHelper<Axis> {
        // used for extending a shaft in its axis, like the piston poles. works with
        // shafts and cogs

        private PlacementHelper() {
            super(
                state -> state.getBlock() instanceof AbstractSimpleShaftBlock || state.getBlock() instanceof PoweredShaftBlock,
                state -> state.get(AXIS),
                AXIS
            );
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return i -> i.getItem() instanceof BlockItem && ((BlockItem) i.getItem()).getBlock() instanceof AbstractSimpleShaftBlock;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return Predicates.or(state -> state.isOf(AllBlocks.SHAFT), state -> state.isOf(AllBlocks.POWERED_SHAFT));
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            PlacementOffset offset = super.getOffset(player, world, state, pos, ray);
            if (offset.isSuccessful())
                offset.withTransform(offset.getTransform()
                    .andThen(s -> world.isClient() ? s : ShaftBlock.pickCorrectShaftType(s, world, offset.getBlockPos())));
            return offset;
        }

    }
}
