package com.zurrtum.create.content.kinetics.crafter;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.ConnectedInputHandler.ConnectedInput;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.CrafterItemHandler;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.Phase;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MechanicalCrafterBlock extends HorizontalKineticBlock implements IBE<MechanicalCrafterBlockEntity>, ICogWheel, ItemInventoryProvider<MechanicalCrafterBlockEntity>, NeighborUpdateListeningBlock {

    public static final EnumProperty<Pointing> POINTING = EnumProperty.create("pointing", Pointing.class);

    public MechanicalCrafterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POINTING, Pointing.UP));
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, MechanicalCrafterBlockEntity blockEntity, Direction context) {
        return blockEntity.getInvCapability();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(POINTING));
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        BlockPos placedOnPos = context.getClickedPos().relative(face.getOpposite());
        BlockState blockState = context.getLevel().getBlockState(placedOnPos);

        if ((blockState.getBlock() != this) || (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())) {
            BlockState stateForPlacement = super.getStateForPlacement(context);
            Direction direction = stateForPlacement.getValue(HORIZONTAL_FACING);
            if (direction != face)
                stateForPlacement = stateForPlacement.setValue(POINTING, pointingFromFacing(face, direction));
            return stateForPlacement;
        }

        Direction otherFacing = blockState.getValue(HORIZONTAL_FACING);
        Pointing pointing = pointingFromFacing(face, otherFacing);
        return defaultBlockState().setValue(HORIZONTAL_FACING, otherFacing).setValue(POINTING, pointing);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.is(this) && getTargetDirection(state) != getTargetDirection(oldState)) {
            MechanicalCrafterBlockEntity crafter = CrafterHelper.getCrafter(worldIn, pos);
            if (crafter != null)
                crafter.blockChanged();
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel worldIn, BlockPos pos, boolean isMoving) {
        if (state.hasBlockEntity()) {
            MechanicalCrafterBlockEntity crafter = CrafterHelper.getCrafter(worldIn, pos);
            if (crafter != null) {
                if (crafter.covered)
                    Block.popResource(worldIn, pos, AllItems.CRAFTER_SLOT_COVER.getDefaultInstance());
                if (!isMoving)
                    crafter.ejectWholeGrid();
            }

            for (Direction direction : Iterate.directions) {
                if (direction.getAxis() == state.getValue(HORIZONTAL_FACING).getAxis())
                    continue;

                BlockPos otherPos = pos.relative(direction);
                ConnectedInput thisInput = CrafterHelper.getInput(worldIn, pos);
                ConnectedInput otherInput = CrafterHelper.getInput(worldIn, otherPos);

                if (thisInput == null || otherInput == null)
                    continue;
                if (!pos.offset(thisInput.data.getFirst()).equals(otherPos.offset(otherInput.data.getFirst())))
                    continue;

                ConnectedInputHandler.toggleConnection(worldIn, pos, otherPos);
            }
        }

        super.affectNeighborsAfterRemoval(state, worldIn, pos, isMoving);
    }

    public static Pointing pointingFromFacing(Direction pointingFace, Direction blockFacing) {
        boolean positive = blockFacing.getAxisDirection() == AxisDirection.POSITIVE;

        Pointing pointing = pointingFace == Direction.DOWN ? Pointing.UP : Pointing.DOWN;
        if (pointingFace == Direction.EAST)
            pointing = positive ? Pointing.LEFT : Pointing.RIGHT;
        if (pointingFace == Direction.WEST)
            pointing = positive ? Pointing.RIGHT : Pointing.LEFT;
        if (pointingFace == Direction.NORTH)
            pointing = positive ? Pointing.LEFT : Pointing.RIGHT;
        if (pointingFace == Direction.SOUTH)
            pointing = positive ? Pointing.RIGHT : Pointing.LEFT;
        return pointing;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getClickedFace() == state.getValue(HORIZONTAL_FACING)) {
            if (!context.getLevel().isClientSide())
                KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(), state.cycle(POINTING));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MechanicalCrafterBlockEntity crafter))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (stack.is(AllItems.MECHANICAL_ARM))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        boolean isHand = stack.isEmpty() && hand == InteractionHand.MAIN_HAND;
        boolean wrenched = stack.is(AllItems.WRENCH);

        if (hitResult.getDirection() == state.getValue(HORIZONTAL_FACING)) {

            if (crafter.phase != Phase.IDLE && !wrenched) {
                crafter.ejectWholeGrid();
                return InteractionResult.SUCCESS;
            }

            if (crafter.phase == Phase.IDLE && !isHand && !wrenched) {
                if (level.isClientSide())
                    return InteractionResult.SUCCESS;

                if (stack.is(AllItems.CRAFTER_SLOT_COVER)) {
                    if (crafter.covered)
                        return InteractionResult.TRY_WITH_EMPTY_HAND;
                    if (!crafter.inventory.isEmpty())
                        return InteractionResult.TRY_WITH_EMPTY_HAND;
                    crafter.covered = true;
                    crafter.setChanged();
                    crafter.sendData();
                    if (!player.isCreative())
                        stack.shrink(1);
                    return InteractionResult.SUCCESS;
                }

                Container capability = crafter.getInvCapability();
                if (capability == null)
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                int count = stack.getCount();
                int insert = capability.insert(stack);
                if (!player.isCreative()) {
                    if (insert == count) {
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    } else if (insert != 0) {
                        stack.setCount(count - insert);
                        player.setItemInHand(hand, stack);
                    }
                }
                return InteractionResult.SUCCESS;
            }

            CrafterItemHandler handler = crafter.getInventory();
            ItemStack inSlot = handler.getStack();
            if (inSlot.isEmpty()) {
                if (crafter.covered && !wrenched) {
                    if (level.isClientSide())
                        return InteractionResult.SUCCESS;
                    crafter.covered = false;
                    crafter.setChanged();
                    crafter.sendData();
                    if (!player.isCreative())
                        player.getInventory().placeItemBackInInventory(AllItems.CRAFTER_SLOT_COVER.getDefaultInstance());
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            if (!isHand && !handler.matches(stack, inSlot))
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            if (level.isClientSide())
                return InteractionResult.SUCCESS;
            player.getInventory().placeItemBackInInventory(handler.onExtract(inSlot));
            handler.setStack(ItemStack.EMPTY);
            handler.setChanged();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public void neighborUpdate(BlockState state, Level worldIn, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
        if (behaviour != null)
            behaviour.onNeighborChanged(fromPos);
    }

    @Override
    public float getParticleTargetRadius() {
        return .85f;
    }

    @Override
    public float getParticleInitialRadius() {
        return .75f;
    }

    public static Direction getTargetDirection(BlockState state) {
        if (!state.is(AllBlocks.MECHANICAL_CRAFTER))
            return Direction.UP;
        Direction facing = state.getValue(HORIZONTAL_FACING);
        Pointing point = state.getValue(POINTING);
        Vec3 targetVec = new Vec3(0, 1, 0);
        targetVec = VecHelper.rotate(targetVec, -point.getXRotation(), Axis.Z);
        targetVec = VecHelper.rotate(targetVec, AngleHelper.horizontalAngle(facing), Axis.Y);
        return Direction.getApproximateNearest(targetVec.x, targetVec.y, targetVec.z);
    }

    public static boolean isValidTarget(Level world, BlockPos targetPos, BlockState crafterState) {
        BlockState targetState = world.getBlockState(targetPos);
        if (!world.isLoaded(targetPos))
            return false;
        if (!targetState.is(AllBlocks.MECHANICAL_CRAFTER))
            return false;
        if (crafterState.getValue(HORIZONTAL_FACING) != targetState.getValue(HORIZONTAL_FACING))
            return false;
        return Math.abs(crafterState.getValue(POINTING).getXRotation() - targetState.getValue(POINTING).getXRotation()) != 180;
    }

    @Override
    public Class<MechanicalCrafterBlockEntity> getBlockEntityClass() {
        return MechanicalCrafterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalCrafterBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.MECHANICAL_CRAFTER;
    }

}
