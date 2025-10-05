package com.zurrtum.create.content.kinetics.simpleRelays;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.decoration.encasing.EncasableBlock;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class CogWheelBlock extends AbstractSimpleShaftBlock implements ICogWheel, EncasableBlock {
    boolean isLarge;

    public CogWheelBlock(boolean large, Settings settings) {
        super(settings);
        isLarge = large;
        setDefaultState(getDefaultState().with(AXIS, Direction.Axis.Y));
    }

    public static CogWheelBlock small(Settings settings) {
        return new CogWheelBlock(false, settings);
    }

    public static CogWheelBlock large(Settings settings) {
        return new CogWheelBlock(true, settings);
    }

    @Override
    public boolean isLargeCog() {
        return isLarge;
    }

    @Override
    public boolean isSmallCog() {
        return !isLarge;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return (isLarge ? AllShapes.LARGE_GEAR : AllShapes.SMALL_GEAR).get(state.get(AXIS));
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        return isValidCogwheelPosition(ICogWheel.isLargeCog(state), worldIn, pos, state.get(AXIS));
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(worldIn, pos, state, placer, stack);
        if (placer instanceof PlayerEntity player)
            triggerShiftingGearsAdvancement(worldIn, pos, state, player);
    }

    protected void triggerShiftingGearsAdvancement(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.isClient() || player == null)
            return;

        Axis axis = state.get(CogWheelBlock.AXIS);
        for (Axis perpendicular1 : Iterate.axes) {
            if (perpendicular1 == axis)
                continue;

            Direction d1 = Direction.get(AxisDirection.POSITIVE, perpendicular1);
            for (Axis perpendicular2 : Iterate.axes) {
                if (perpendicular1 == perpendicular2)
                    continue;
                if (axis == perpendicular2)
                    continue;

                Direction d2 = Direction.get(AxisDirection.POSITIVE, perpendicular2);
                for (int offset1 : Iterate.positiveAndNegative) {
                    for (int offset2 : Iterate.positiveAndNegative) {
                        BlockPos connectedPos = pos.offset(d1, offset1).offset(d2, offset2);
                        BlockState blockState = world.getBlockState(connectedPos);
                        if (!(blockState.getBlock() instanceof CogWheelBlock))
                            continue;
                        if (blockState.get(CogWheelBlock.AXIS) != axis)
                            continue;
                        if (ICogWheel.isLargeCog(blockState) == isLarge)
                            continue;

                        AllAdvancements.COGS.trigger((ServerPlayerEntity) player);
                    }
                }
            }
        }
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

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    public static boolean isValidCogwheelPosition(boolean large, WorldView worldIn, BlockPos pos, Axis cogAxis) {
        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() == cogAxis)
                continue;

            BlockPos offsetPos = pos.offset(facing);
            BlockState blockState = worldIn.getBlockState(offsetPos);
            if (blockState.contains(AXIS) && facing.getAxis() == blockState.get(AXIS))
                continue;

            if (ICogWheel.isLargeCog(blockState) || large && ICogWheel.isSmallCog(blockState))
                return false;
        }
        return true;
    }

    protected Axis getAxisForPlacement(ItemPlacementContext context) {
        if (context.getPlayer() != null && context.getPlayer().isSneaking())
            return context.getSide().getAxis();

        World world = context.getWorld();
        BlockState stateBelow = world.getBlockState(context.getBlockPos().down());

        if (stateBelow.isOf(AllBlocks.ROTATION_SPEED_CONTROLLER) && isLargeCog())
            return stateBelow.get(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X;

        BlockPos placedOnPos = context.getBlockPos().offset(context.getSide().getOpposite());
        BlockState placedAgainst = world.getBlockState(placedOnPos);

        Block block = placedAgainst.getBlock();
        if (ICogWheel.isSmallCog(placedAgainst))
            return ((IRotate) block).getRotationAxis(placedAgainst);

        Axis preferredAxis = getPreferredAxis(context);
        return preferredAxis != null ? preferredAxis : context.getSide().getAxis();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        boolean shouldWaterlog = context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER;
        return this.getDefaultState().with(AXIS, getAxisForPlacement(context)).with(Properties.WATERLOGGED, shouldWaterlog);
    }

    @Override
    public float getParticleTargetRadius() {
        return isLargeCog() ? 1.125f : .65f;
    }

    @Override
    public float getParticleInitialRadius() {
        return isLargeCog() ? 1f : .75f;
    }

    @Override
    public boolean isDedicatedCogWheel() {
        return true;
    }
}
