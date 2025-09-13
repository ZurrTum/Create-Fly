package com.zurrtum.create.content.logistics.tunnel;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerSidedFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BrassTunnelBlockEntity extends BeltTunnelBlockEntity {
    ServerSidedFilteringBehaviour filtering;

    boolean connectedLeft;
    boolean connectedRight;

    ItemStack stackToDistribute;
    Direction stackEnteredFrom;

    float distributionProgress;
    int distributionDistanceLeft;
    int distributionDistanceRight;
    int previousOutputIndex;

    // <filtered, non-filtered>
    Couple<List<Pair<BlockPos, Direction>>> distributionTargets;

    private boolean newItemArrived;
    private boolean syncedOutputActive;
    private Set<BrassTunnelBlockEntity> syncSet;

    protected ServerScrollOptionBehaviour<SelectionMode> selectionMode;
    private Inventory beltCapability;
    public Inventory tunnelCapability;

    public BrassTunnelBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.BRASS_TUNNEL, pos, state);
        distributionTargets = Couple.create(ArrayList::new);
        syncSet = new HashSet<>();
        stackToDistribute = ItemStack.EMPTY;
        stackEnteredFrom = null;
        beltCapability = null;
        tunnelCapability = new BrassTunnelItemHandler(this);
        previousOutputIndex = 0;
        syncedOutputActive = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(selectionMode = new ServerScrollOptionBehaviour<>(SelectionMode.class, this));

        // Propagate settings across connected tunnels
        selectionMode.withCallback(setting -> {
            for (boolean side : Iterate.trueAndFalse) {
                if (!isConnected(side))
                    continue;
                BrassTunnelBlockEntity adjacent = getAdjacent(side);
                if (adjacent != null)
                    adjacent.selectionMode.setValue(setting);
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        BeltBlockEntity beltBelow = BeltHelper.getSegmentBE(world, pos.down());

        if (distributionProgress > 0)
            distributionProgress--;
        if (beltBelow == null || beltBelow.getSpeed() == 0)
            return;
        if (stackToDistribute.isEmpty() && !syncedOutputActive)
            return;
        if (world.isClient && !isVirtual())
            return;

        if (distributionProgress == -1) {
            distributionTargets.forEach(List::clear);
            distributionDistanceLeft = 0;
            distributionDistanceRight = 0;

            syncSet.clear();
            List<Pair<BrassTunnelBlockEntity, Direction>> validOutputs = gatherValidOutputs();
            if (selectionMode.get() == SelectionMode.SYNCHRONIZE) {
                boolean allEmpty = true;
                boolean allFull = true;
                for (BrassTunnelBlockEntity be : syncSet) {
                    boolean hasStack = !be.stackToDistribute.isEmpty();
                    allEmpty &= !hasStack;
                    allFull &= hasStack;
                }
                final boolean notifySyncedOut = !allEmpty;
                if (allFull || allEmpty)
                    syncSet.forEach(be -> be.syncedOutputActive = notifySyncedOut);
            }

            if (validOutputs == null)
                return;
            if (stackToDistribute.isEmpty())
                return;

            for (Pair<BrassTunnelBlockEntity, Direction> pair : validOutputs) {
                BrassTunnelBlockEntity tunnel = pair.getFirst();
                Direction output = pair.getSecond();
                if (insertIntoTunnel(tunnel, output, stackToDistribute, true) == null)
                    continue;
                distributionTargets.get(!tunnel.flapFilterEmpty(output)).add(Pair.of(tunnel.pos, output));
                int distance = tunnel.pos.getX() + tunnel.pos.getZ() - pos.getX() - pos.getZ();
                if (distance < 0)
                    distributionDistanceLeft = Math.max(distributionDistanceLeft, -distance);
                else
                    distributionDistanceRight = Math.max(distributionDistanceRight, distance);
            }

            if (distributionTargets.getFirst().isEmpty() && distributionTargets.getSecond().isEmpty())
                return;

            if (newItemArrived) {
                newItemArrived = false;
                distributionProgress = 2;
            } else {
                if (selectionMode.get() != SelectionMode.SYNCHRONIZE || syncedOutputActive) {
                    distributionProgress = AllConfigs.server().logistics.brassTunnelTimer.get();
                    sendData();
                }
                return;
            }
        }

        if (distributionProgress != 0)
            return;

        distributionTargets.forEach(list -> {
            if (stackToDistribute.isEmpty())
                return;
            List<Pair<BrassTunnelBlockEntity, Direction>> validTargets = new ArrayList<>();
            for (Pair<BlockPos, Direction> pair : list) {
                BlockPos tunnelPos = pair.getFirst();
                Direction output = pair.getSecond();
                if (tunnelPos.equals(pos) && output == stackEnteredFrom)
                    continue;
                BlockEntity be = world.getBlockEntity(tunnelPos);
                if (!(be instanceof BrassTunnelBlockEntity))
                    continue;
                validTargets.add(Pair.of((BrassTunnelBlockEntity) be, output));
            }
            distribute(validTargets);
            distributionProgress = -1;
        });
    }

    private static Map<Pair<BrassTunnelBlockEntity, Direction>, ItemStack> distributed = new IdentityHashMap<>();
    private static Set<Pair<BrassTunnelBlockEntity, Direction>> full = new HashSet<>();

    private void distribute(List<Pair<BrassTunnelBlockEntity, Direction>> validTargets) {
        int amountTargets = validTargets.size();
        if (amountTargets == 0)
            return;

        distributed.clear();
        full.clear();

        int indexStart = previousOutputIndex % amountTargets;
        SelectionMode mode = selectionMode.get();
        boolean force = mode == SelectionMode.FORCED_ROUND_ROBIN || mode == SelectionMode.FORCED_SPLIT;
        boolean split = mode == SelectionMode.FORCED_SPLIT || mode == SelectionMode.SPLIT;
        boolean robin = mode == SelectionMode.FORCED_ROUND_ROBIN || mode == SelectionMode.ROUND_ROBIN;

        if (mode == SelectionMode.RANDOMIZE)
            indexStart = world.random.nextInt(amountTargets);
        if (mode == SelectionMode.PREFER_NEAREST || mode == SelectionMode.SYNCHRONIZE)
            indexStart = 0;

        ItemStack toDistribute = stackToDistribute.copy();
        for (boolean distributeAgain : Iterate.trueAndFalse) {
            ItemStack toDistributeThisCycle = null;
            int remainingOutputs = amountTargets;
            int leftovers = 0;

            for (boolean simulate : Iterate.trueAndFalse) {
                if (remainingOutputs == 0)
                    break;

                leftovers = 0;
                int index = indexStart;
                int stackSize = toDistribute.getCount();
                int splitStackSize = stackSize / remainingOutputs;
                int splitRemainder = stackSize % remainingOutputs;
                int visited = 0;

                toDistributeThisCycle = toDistribute.copy();
                if (!(force || split) && simulate)
                    continue;

                while (visited < amountTargets) {
                    Pair<BrassTunnelBlockEntity, Direction> pair = validTargets.get(index);
                    BrassTunnelBlockEntity tunnel = pair.getFirst();
                    Direction side = pair.getSecond();
                    index = (index + 1) % amountTargets;
                    visited++;

                    if (full.contains(pair)) {
                        if (split && simulate)
                            remainingOutputs--;
                        continue;
                    }

                    int count = split ? splitStackSize + (splitRemainder > 0 ? 1 : 0) : stackSize;
                    ItemStack toOutput = toDistributeThisCycle.copyWithCount(count);

                    // Grow by 1 to determine if target is full even after a successful transfer
                    boolean testWithIncreasedCount = distributed.containsKey(pair);
                    int increasedCount = testWithIncreasedCount ? distributed.get(pair).getCount() : 0;
                    if (testWithIncreasedCount)
                        toOutput.increment(increasedCount);

                    ItemStack remainder = insertIntoTunnel(tunnel, side, toOutput, true);

                    if (remainder == null || remainder.getCount() == (testWithIncreasedCount ? count + 1 : count)) {
                        if (force)
                            return;
                        if (split && simulate)
                            remainingOutputs--;
                        if (!simulate)
                            full.add(pair);
                        if (robin)
                            break;
                        continue;
                    } else if (!remainder.isEmpty() && !simulate) {
                        full.add(pair);
                    }

                    if (!simulate) {
                        toOutput.decrement(remainder.getCount());
                        distributed.put(pair, toOutput);
                    }

                    leftovers += remainder.getCount();
                    toDistributeThisCycle.decrement(count);
                    if (toDistributeThisCycle.isEmpty())
                        break;
                    splitRemainder--;
                    if (!split)
                        break;
                }
            }

            toDistribute.setCount(toDistributeThisCycle.getCount() + leftovers);
            if (leftovers == 0 && distributeAgain)
                break;
            if (!split)
                break;
        }

        int failedTransferrals = 0;
        for (Map.Entry<Pair<BrassTunnelBlockEntity, Direction>, ItemStack> entry : distributed.entrySet()) {
            Pair<BrassTunnelBlockEntity, Direction> pair = entry.getKey();
            failedTransferrals += insertIntoTunnel(pair.getFirst(), pair.getSecond(), entry.getValue(), false).getCount();
        }

        toDistribute.increment(failedTransferrals);
        stackToDistribute = stackToDistribute.copyWithCount(toDistribute.getCount());
        if (stackToDistribute.isEmpty())
            stackEnteredFrom = null;
        previousOutputIndex++;
        previousOutputIndex %= amountTargets;
        notifyUpdate();
    }

    public void setStackToDistribute(ItemStack stack, @Nullable Direction enteredFrom) {
        stackToDistribute = stack;
        stackEnteredFrom = enteredFrom;
        distributionProgress = -1;
        if (!stack.isEmpty())
            newItemArrived = true;
        sendData();
        markDirty();
    }

    public ItemStack getStackToDistribute() {
        return stackToDistribute;
    }

    public List<ItemStack> grabAllStacksOfGroup(boolean simulate) {
        List<ItemStack> list = new ArrayList<>();

        ItemStack own = getStackToDistribute();
        if (!own.isEmpty()) {
            list.add(own);
            if (!simulate)
                setStackToDistribute(ItemStack.EMPTY, null);
        }

        for (boolean left : Iterate.trueAndFalse) {
            BrassTunnelBlockEntity adjacent = this;
            while (adjacent != null) {
                if (!world.isPosLoaded(adjacent.getPos()))
                    return null;
                adjacent = adjacent.getAdjacent(left);
                if (adjacent == null)
                    continue;
                ItemStack other = adjacent.getStackToDistribute();
                if (other.isEmpty())
                    continue;
                list.add(other);
                if (!simulate)
                    adjacent.setStackToDistribute(ItemStack.EMPTY, null);
            }
        }

        return list;
    }

    @Nullable
    protected ItemStack insertIntoTunnel(BrassTunnelBlockEntity tunnel, Direction side, ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return stack;
        if (!tunnel.testFlapFilter(side, stack))
            return null;

        BeltBlockEntity below = BeltHelper.getSegmentBE(world, tunnel.pos.down());
        if (below == null)
            return null;
        BlockPos offset = tunnel.getPos().down().offset(side);
        DirectBeltInputBehaviour sideOutput = BlockEntityBehaviour.get(world, offset, DirectBeltInputBehaviour.TYPE);
        if (sideOutput != null) {
            if (!sideOutput.canInsertFromSide(side))
                return null;
            ItemStack result = sideOutput.handleInsertion(stack, side, simulate);
            if (result.isEmpty() && !simulate)
                tunnel.flap(side, false);
            return result;
        }

        Direction movementFacing = below.getMovementFacing();
        if (side == movementFacing)
            if (!BlockHelper.hasBlockSolidSide(world.getBlockState(offset), world, offset, side.getOpposite())) {
                BeltBlockEntity controllerBE = below.getControllerBE();
                if (controllerBE == null)
                    return null;

                if (!simulate) {
                    tunnel.flap(side, true);
                    ItemStack ejected = stack;
                    float beltMovementSpeed = below.getDirectionAwareBeltMovementSpeed();
                    float movementSpeed = Math.max(Math.abs(beltMovementSpeed), 1 / 8f);
                    int additionalOffset = beltMovementSpeed > 0 ? 1 : 0;
                    Vec3d outPos = BeltHelper.getVectorForOffset(controllerBE, below.index + additionalOffset);
                    Vec3d outMotion = Vec3d.of(side.getVector()).multiply(movementSpeed).add(0, 1 / 8f, 0);
                    outPos.add(outMotion.normalize());
                    ItemEntity entity = new ItemEntity(world, outPos.x, outPos.y + 6 / 16f, outPos.z, ejected);
                    entity.setVelocity(outMotion);
                    entity.setToDefaultPickupDelay();
                    entity.velocityModified = true;
                    world.spawnEntity(entity);
                }

                return ItemStack.EMPTY;
            }

        return null;
    }

    public boolean testFlapFilter(Direction side, ItemStack stack) {
        if (filtering == null)
            return false;
        if (filtering.get(side) == null) {
            ServerFilteringBehaviour adjacentFilter = BlockEntityBehaviour.get(world, pos.offset(side), ServerFilteringBehaviour.TYPE);
            if (adjacentFilter == null)
                return true;
            return adjacentFilter.test(stack);
        }
        return filtering.test(side, stack);
    }

    public boolean flapFilterEmpty(Direction side) {
        if (filtering == null)
            return false;
        if (filtering.get(side) == null) {
            ServerFilteringBehaviour adjacentFilter = BlockEntityBehaviour.get(world, pos.offset(side), ServerFilteringBehaviour.TYPE);
            if (adjacentFilter == null)
                return true;
            return adjacentFilter.getFilter().isEmpty();
        }
        return filtering.getFilter(side).isEmpty();
    }

    @Override
    public void initialize() {
        if (filtering == null) {
            filtering = createSidedFilter();
            attachBehaviourLate(filtering);
        }
        super.initialize();
    }

    public boolean canInsert(Direction side, ItemStack stack) {
        if (filtering != null && !filtering.test(side, stack))
            return false;
        if (!hasDistributionBehaviour())
            return true;
        return stackToDistribute.isEmpty();
    }

    public boolean hasDistributionBehaviour() {
        if (flaps.isEmpty())
            return false;
        if (connectedLeft || connectedRight)
            return true;
        BlockState blockState = getCachedState();
        if (!blockState.isOf(AllBlocks.BRASS_TUNNEL))
            return false;
        Axis axis = blockState.get(BrassTunnelBlock.HORIZONTAL_AXIS);
        for (Direction direction : flaps.keySet())
            if (direction.getAxis() != axis)
                return true;
        return false;
    }

    private List<Pair<BrassTunnelBlockEntity, Direction>> gatherValidOutputs() {
        List<Pair<BrassTunnelBlockEntity, Direction>> validOutputs = new ArrayList<>();
        boolean synchronize = selectionMode.get() == SelectionMode.SYNCHRONIZE;
        addValidOutputsOf(this, validOutputs);

        for (boolean left : Iterate.trueAndFalse) {
            BrassTunnelBlockEntity adjacent = this;
            while (adjacent != null) {
                if (!world.isPosLoaded(adjacent.getPos()))
                    return null;
                adjacent = adjacent.getAdjacent(left);
                if (adjacent == null)
                    continue;
                addValidOutputsOf(adjacent, validOutputs);
            }
        }

        if (!syncedOutputActive && synchronize)
            return null;
        return validOutputs;
    }

    private void addValidOutputsOf(BrassTunnelBlockEntity tunnelBE, List<Pair<BrassTunnelBlockEntity, Direction>> validOutputs) {
        syncSet.add(tunnelBE);
        BeltBlockEntity below = BeltHelper.getSegmentBE(world, tunnelBE.pos.down());
        if (below == null)
            return;
        Direction movementFacing = below.getMovementFacing();
        BlockState blockState = getCachedState();
        if (!blockState.isOf(AllBlocks.BRASS_TUNNEL))
            return;

        boolean prioritizeSides = tunnelBE == this;

        for (boolean sidePass : Iterate.trueAndFalse) {
            if (!prioritizeSides && sidePass)
                continue;
            for (Direction direction : Iterate.horizontalDirections) {
                if (direction == movementFacing && below.getSpeed() == 0)
                    continue;
                if (prioritizeSides && sidePass == (direction.getAxis() == movementFacing.getAxis()))
                    continue;
                if (direction == movementFacing.getOpposite())
                    continue;
                if (!tunnelBE.sides.contains(direction))
                    continue;

                BlockPos offset = tunnelBE.pos.down().offset(direction);

                BlockState potentialFunnel = world.getBlockState(offset.up());
                if (potentialFunnel.getBlock() instanceof BeltFunnelBlock && potentialFunnel.get(BeltFunnelBlock.SHAPE) == Shape.PULLING && FunnelBlock.getFunnelFacing(
                    potentialFunnel) == direction)
                    continue;

                DirectBeltInputBehaviour inputBehaviour = BlockEntityBehaviour.get(world, offset, DirectBeltInputBehaviour.TYPE);
                if (inputBehaviour == null) {
                    if (direction == movementFacing)
                        if (!BlockHelper.hasBlockSolidSide(world.getBlockState(offset), world, offset, direction.getOpposite()))
                            validOutputs.add(Pair.of(tunnelBE, direction));
                    continue;
                }
                if (inputBehaviour.canInsertFromSide(direction))
                    validOutputs.add(Pair.of(tunnelBE, direction));
            }
        }
    }

    @Override
    public void addBehavioursDeferred(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehavioursDeferred(behaviours);
        filtering = createSidedFilter();
        behaviours.add(filtering);
    }

    protected ServerSidedFilteringBehaviour createSidedFilter() {
        return new ServerSidedFilteringBehaviour(this, this::makeFilter, this::isValidFaceForFilter);
    }

    private ServerFilteringBehaviour makeFilter(Direction side, ServerFilteringBehaviour filter) {
        return filter;
    }

    private boolean isValidFaceForFilter(Direction side) {
        return sides.contains(side);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putBoolean("SyncedOutput", syncedOutputActive);
        view.putBoolean("ConnectedLeft", connectedLeft);
        view.putBoolean("ConnectedRight", connectedRight);

        if (!stackToDistribute.isEmpty()) {
            view.put("StackToDistribute", ItemStack.CODEC, stackToDistribute);
        }
        if (stackEnteredFrom != null)
            view.put("StackEnteredFrom", Direction.CODEC, stackEnteredFrom);

        view.putFloat("DistributionProgress", distributionProgress);
        view.putInt("PreviousIndex", previousOutputIndex);
        view.putInt("DistanceLeft", distributionDistanceLeft);
        view.putInt("DistanceRight", distributionDistanceRight);

        view.put("FilteredTargets", CreateCodecs.BLOCK_POS_DIRECTION_LIST_CODEC, distributionTargets.getFirst());
        view.put("Targets", CreateCodecs.BLOCK_POS_DIRECTION_LIST_CODEC, distributionTargets.getSecond());

        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        boolean wasConnectedLeft = connectedLeft;
        boolean wasConnectedRight = connectedRight;

        syncedOutputActive = view.getBoolean("SyncedOutput", false);
        connectedLeft = view.getBoolean("ConnectedLeft", false);
        connectedRight = view.getBoolean("ConnectedRight", false);

        stackToDistribute = view.read("StackToDistribute", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        stackEnteredFrom = view.read("StackEnteredFrom", Direction.CODEC).orElse(null);

        distributionProgress = view.getFloat("DistributionProgress", 0);
        previousOutputIndex = view.getInt("PreviousIndex", 0);
        distributionDistanceLeft = view.getInt("DistanceLeft", 0);
        distributionDistanceRight = view.getInt("DistanceRight", 0);

        distributionTargets.getFirst().clear();
        view.read("FilteredTargets", CreateCodecs.BLOCK_POS_DIRECTION_LIST_CODEC)
            .ifPresent(targets -> distributionTargets.getFirst().addAll(targets));
        distributionTargets.getSecond().clear();
        view.read("Targets", CreateCodecs.BLOCK_POS_DIRECTION_LIST_CODEC).ifPresent(targets -> distributionTargets.getSecond().addAll(targets));

        super.read(view, clientPacket);

        if (!clientPacket)
            return;
        if (wasConnectedLeft != connectedLeft || wasConnectedRight != connectedRight) {
            if (hasWorld())
                world.updateListeners(getPos(), getCachedState(), getCachedState(), 16);
        }
        filtering.updateFilterPresence();
    }

    public boolean isConnected(boolean leftSide) {
        return leftSide ? connectedLeft : connectedRight;
    }

    @Override
    public void updateTunnelConnections() {
        super.updateTunnelConnections();
        boolean connectivityChanged = false;
        boolean nowConnectedLeft = determineIfConnected(true);
        boolean nowConnectedRight = determineIfConnected(false);

        if (connectedLeft != nowConnectedLeft) {
            connectedLeft = nowConnectedLeft;
            connectivityChanged = true;
            BrassTunnelBlockEntity adjacent = getAdjacent(true);
            if (adjacent != null && !world.isClient) {
                adjacent.updateTunnelConnections();
                adjacent.selectionMode.setValue(selectionMode.getValue());
            }
        }

        if (connectedRight != nowConnectedRight) {
            connectedRight = nowConnectedRight;
            connectivityChanged = true;
            BrassTunnelBlockEntity adjacent = getAdjacent(false);
            if (adjacent != null && !world.isClient) {
                adjacent.updateTunnelConnections();
                adjacent.selectionMode.setValue(selectionMode.getValue());
            }
        }

        if (filtering != null)
            filtering.updateFilterPresence();
        if (connectivityChanged)
            sendData();
    }

    protected boolean determineIfConnected(boolean leftSide) {
        if (flaps.isEmpty())
            return false;
        BrassTunnelBlockEntity adjacentTunnelBE = getAdjacent(leftSide);
        return adjacentTunnelBE != null && !adjacentTunnelBE.flaps.isEmpty();
    }

    @Nullable
    protected BrassTunnelBlockEntity getAdjacent(boolean leftSide) {
        if (!hasWorld())
            return null;

        BlockState blockState = getCachedState();
        if (!blockState.isOf(AllBlocks.BRASS_TUNNEL))
            return null;

        Axis axis = blockState.get(BrassTunnelBlock.HORIZONTAL_AXIS);
        Direction baseDirection = Direction.get(AxisDirection.POSITIVE, axis);
        Direction direction = leftSide ? baseDirection.rotateYCounterclockwise() : baseDirection.rotateYClockwise();
        BlockPos adjacentPos = pos.offset(direction);
        BlockState adjacentBlockState = world.getBlockState(adjacentPos);

        if (!adjacentBlockState.isOf(AllBlocks.BRASS_TUNNEL))
            return null;
        if (adjacentBlockState.get(BrassTunnelBlock.HORIZONTAL_AXIS) != axis)
            return null;
        BlockEntity adjacentBE = world.getBlockEntity(adjacentPos);
        if (adjacentBE.isRemoved())
            return null;
        if (!(adjacentBE instanceof BrassTunnelBlockEntity))
            return null;
        return (BrassTunnelBlockEntity) adjacentBE;
    }

    @Override
    public void destroy() {
        super.destroy();
        Block.dropStack(world, pos, stackToDistribute);
        stackEnteredFrom = null;
    }

    public Inventory getBeltCapability() {
        if (beltCapability == null) {
            BlockPos down = pos.down();
            BlockEntity blockEntity = world.getBlockEntity(down);
            if (blockEntity != null) {
                beltCapability = ItemHelper.getInventory(world, down, null, blockEntity, Direction.UP);
            }
        }
        return beltCapability;
    }

    public enum SelectionMode {
        SPLIT,
        FORCED_SPLIT,
        ROUND_ROBIN,
        FORCED_ROUND_ROBIN,
        PREFER_NEAREST,
        RANDOMIZE,
        SYNCHRONIZE;
    }

    public boolean canTakeItems() {
        return stackToDistribute.isEmpty() && !syncedOutputActive;
    }
}
