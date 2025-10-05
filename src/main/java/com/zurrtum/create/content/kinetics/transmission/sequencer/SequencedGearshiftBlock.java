package com.zurrtum.create.content.kinetics.transmission.sequencer;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlock;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class SequencedGearshiftBlock extends HorizontalAxisKineticBlock implements IBE<SequencedGearshiftBlockEntity>, TransformableBlock, WeakPowerControlBlock {
    public static final BooleanProperty VERTICAL = BooleanProperty.of("vertical");
    public static final IntProperty STATE = IntProperty.of("state", 0, 5);

    public SequencedGearshiftBlock(AbstractBlock.Settings properties) {
        super(properties);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(STATE, VERTICAL));
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World level,
        BlockPos pos,
        Block block,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        if (level.isClient())
            return;
        if (!level.getBlockTickScheduler().isTicking(pos, this))
            level.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random r) {
        boolean previouslyPowered = state.get(STATE) != 0;
        boolean isPowered = worldIn.isReceivingRedstonePower(pos);
        withBlockEntityDo(worldIn, pos, sgte -> sgte.onRedstoneUpdate(isPowered, previouslyPowered));
    }

    @Override
    protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        return false;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        if (state.get(VERTICAL))
            return face.getAxis().isVertical();
        return super.hasShaftTowards(world, pos, state, face);
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
        if (stack.isOf(AllItems.WRENCH))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (stack.getItem() instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof KineticBlock && hasShaftTowards(level, pos, state, hitResult.getSide()))
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }

        if (level.isClient()) {
            withBlockEntityDo(level, pos, be -> AllClientHandle.INSTANCE.openSequencedGearshiftScreen(be));
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Axis preferredAxis = RotatedPillarKineticBlock.getPreferredAxis(context);
        if (preferredAxis != null && (context.getPlayer() == null || !context.getPlayer().isSneaking()))
            return withAxis(preferredAxis, context);
        return withAxis(context.getPlayerLookDirection().getAxis(), context);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        BlockState newState = state;

        if (context.getSide().getAxis() != Axis.Y)
            if (newState.get(HORIZONTAL_AXIS) != context.getSide().getAxis())
                newState = newState.cycle(VERTICAL);

        return super.onWrenched(newState, context);
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side) {
        return false;
    }

    private BlockState withAxis(Axis axis, ItemPlacementContext context) {
        BlockState state = getDefaultState().with(VERTICAL, axis.isVertical());
        if (axis.isVertical())
            return state.with(HORIZONTAL_AXIS, context.getHorizontalPlayerFacing().getAxis());
        return state.with(HORIZONTAL_AXIS, axis);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        if (state.get(VERTICAL))
            return Axis.Y;
        return super.getRotationAxis(state);
    }

    @Override
    public Class<SequencedGearshiftBlockEntity> getBlockEntityClass() {
        return SequencedGearshiftBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SequencedGearshiftBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.SEQUENCED_GEARSHIFT;
    }

    @Override
    public boolean hasComparatorOutput(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return state.get(STATE);
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        if (transform.mirror != null) {
            state = mirror(state, transform.mirror);
        }

        if (transform.rotationAxis == Axis.Y) {
            return rotate(state, transform.rotation);
        }

        if (transform.rotation.ordinal() % 2 == 1) {
            if (transform.rotationAxis != state.get(HORIZONTAL_AXIS)) {
                return state.cycle(VERTICAL);
            } else if (state.get(VERTICAL)) {
                return state.cycle(VERTICAL).cycle(HORIZONTAL_AXIS);
            }
        }
        return state;
    }

}