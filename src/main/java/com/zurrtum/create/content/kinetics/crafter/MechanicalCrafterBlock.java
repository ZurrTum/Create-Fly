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
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class MechanicalCrafterBlock extends HorizontalKineticBlock implements IBE<MechanicalCrafterBlockEntity>, ICogWheel, ItemInventoryProvider<MechanicalCrafterBlockEntity>, NeighborUpdateListeningBlock {

    public static final EnumProperty<Pointing> POINTING = EnumProperty.of("pointing", Pointing.class);

    public MechanicalCrafterBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POINTING, Pointing.UP));
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, MechanicalCrafterBlockEntity blockEntity, Direction context) {
        return blockEntity.getInvCapability();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POINTING));
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction face = context.getSide();
        BlockPos placedOnPos = context.getBlockPos().offset(face.getOpposite());
        BlockState blockState = context.getWorld().getBlockState(placedOnPos);

        if ((blockState.getBlock() != this) || (context.getPlayer() != null && context.getPlayer().isSneaking())) {
            BlockState stateForPlacement = super.getPlacementState(context);
            Direction direction = stateForPlacement.get(HORIZONTAL_FACING);
            if (direction != face)
                stateForPlacement = stateForPlacement.with(POINTING, pointingFromFacing(face, direction));
            return stateForPlacement;
        }

        Direction otherFacing = blockState.get(HORIZONTAL_FACING);
        Pointing pointing = pointingFromFacing(face, otherFacing);
        return getDefaultState().with(HORIZONTAL_FACING, otherFacing).with(POINTING, pointing);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.isOf(this) && getTargetDirection(state) != getTargetDirection(oldState)) {
            MechanicalCrafterBlockEntity crafter = CrafterHelper.getCrafter(worldIn, pos);
            if (crafter != null)
                crafter.blockChanged();
        }
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld worldIn, BlockPos pos, boolean isMoving) {
        if (state.hasBlockEntity()) {
            MechanicalCrafterBlockEntity crafter = CrafterHelper.getCrafter(worldIn, pos);
            if (crafter != null) {
                if (crafter.covered)
                    Block.dropStack(worldIn, pos, AllItems.CRAFTER_SLOT_COVER.getDefaultStack());
                if (!isMoving)
                    crafter.ejectWholeGrid();
            }

            for (Direction direction : Iterate.directions) {
                if (direction.getAxis() == state.get(HORIZONTAL_FACING).getAxis())
                    continue;

                BlockPos otherPos = pos.offset(direction);
                ConnectedInput thisInput = CrafterHelper.getInput(worldIn, pos);
                ConnectedInput otherInput = CrafterHelper.getInput(worldIn, otherPos);

                if (thisInput == null || otherInput == null)
                    continue;
                if (!pos.add(thisInput.data.getFirst()).equals(otherPos.add(otherInput.data.getFirst())))
                    continue;

                ConnectedInputHandler.toggleConnection(worldIn, pos, otherPos);
            }
        }

        super.onStateReplaced(state, worldIn, pos, isMoving);
    }

    public static Pointing pointingFromFacing(Direction pointingFace, Direction blockFacing) {
        boolean positive = blockFacing.getDirection() == AxisDirection.POSITIVE;

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
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        if (context.getSide() == state.get(HORIZONTAL_FACING)) {
            if (!context.getWorld().isClient())
                KineticBlockEntity.switchToBlockState(context.getWorld(), context.getBlockPos(), state.cycle(POINTING));
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
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
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MechanicalCrafterBlockEntity crafter))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (stack.isOf(AllItems.MECHANICAL_ARM))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        boolean isHand = stack.isEmpty() && hand == Hand.MAIN_HAND;
        boolean wrenched = stack.isOf(AllItems.WRENCH);

        if (hitResult.getSide() == state.get(HORIZONTAL_FACING)) {

            if (crafter.phase != Phase.IDLE && !wrenched) {
                crafter.ejectWholeGrid();
                return ActionResult.SUCCESS;
            }

            if (crafter.phase == Phase.IDLE && !isHand && !wrenched) {
                if (level.isClient())
                    return ActionResult.SUCCESS;

                if (stack.isOf(AllItems.CRAFTER_SLOT_COVER)) {
                    if (crafter.covered)
                        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                    if (!crafter.inventory.isEmpty())
                        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                    crafter.covered = true;
                    crafter.markDirty();
                    crafter.sendData();
                    if (!player.isCreative())
                        stack.decrement(1);
                    return ActionResult.SUCCESS;
                }

                Inventory capability = crafter.getInvCapability();
                if (capability == null)
                    return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                int count = stack.getCount();
                int insert = capability.insert(stack);
                if (!player.isCreative()) {
                    if (insert == count) {
                        player.setStackInHand(hand, ItemStack.EMPTY);
                    } else if (insert != 0) {
                        stack.setCount(count - insert);
                        player.setStackInHand(hand, stack);
                    }
                }
                return ActionResult.SUCCESS;
            }

            CrafterItemHandler handler = crafter.getInventory();
            ItemStack inSlot = handler.getStack();
            if (inSlot.isEmpty()) {
                if (crafter.covered && !wrenched) {
                    if (level.isClient())
                        return ActionResult.SUCCESS;
                    crafter.covered = false;
                    crafter.markDirty();
                    crafter.sendData();
                    if (!player.isCreative())
                        player.getInventory().offerOrDrop(AllItems.CRAFTER_SLOT_COVER.getDefaultStack());
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            }
            if (!isHand && !handler.matches(stack, inSlot))
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            if (level.isClient())
                return ActionResult.SUCCESS;
            player.getInventory().offerOrDrop(handler.onExtract(inSlot));
            handler.setStack(ItemStack.EMPTY);
            handler.markDirty();
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
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
        if (!state.isOf(AllBlocks.MECHANICAL_CRAFTER))
            return Direction.UP;
        Direction facing = state.get(HORIZONTAL_FACING);
        Pointing point = state.get(POINTING);
        Vec3d targetVec = new Vec3d(0, 1, 0);
        targetVec = VecHelper.rotate(targetVec, -point.getXRotation(), Axis.Z);
        targetVec = VecHelper.rotate(targetVec, AngleHelper.horizontalAngle(facing), Axis.Y);
        return Direction.getFacing(targetVec.x, targetVec.y, targetVec.z);
    }

    public static boolean isValidTarget(World world, BlockPos targetPos, BlockState crafterState) {
        BlockState targetState = world.getBlockState(targetPos);
        if (!world.isPosLoaded(targetPos))
            return false;
        if (!targetState.isOf(AllBlocks.MECHANICAL_CRAFTER))
            return false;
        if (crafterState.get(HORIZONTAL_FACING) != targetState.get(HORIZONTAL_FACING))
            return false;
        return Math.abs(crafterState.get(POINTING).getXRotation() - targetState.get(POINTING).getXRotation()) != 180;
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
