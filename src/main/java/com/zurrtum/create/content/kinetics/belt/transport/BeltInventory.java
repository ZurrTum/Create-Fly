package com.zurrtum.create.content.kinetics.belt.transport;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class BeltInventory {

    final BeltBlockEntity belt;
    private final List<TransportedItemStack> items;
    final List<TransportedItemStack> toInsert;
    final List<TransportedItemStack> toRemove;
    boolean beltMovementPositive;

    TransportedItemStack lazyClientItem;

    public BeltInventory(BeltBlockEntity be) {
        this.belt = be;
        items = new LinkedList<>();
        toInsert = new LinkedList<>();
        toRemove = new LinkedList<>();
    }

    public void tick() {

        // Residual item for "smooth" transitions
        if (lazyClientItem != null) {
            if (lazyClientItem.locked)
                lazyClientItem = null;
            else
                lazyClientItem.locked = true;
        }

        // Added/Removed items from previous cycle
        if (!toInsert.isEmpty() || !toRemove.isEmpty()) {
            toInsert.forEach(this::insert);
            toInsert.clear();
            items.removeAll(toRemove);
            toRemove.clear();
            belt.notifyUpdate();
        }

        if (belt.getSpeed() == 0)
            return;

        // Reverse item collection if belt just reversed
        if (beltMovementPositive != belt.getDirectionAwareBeltMovementSpeed() > 0) {
            beltMovementPositive = !beltMovementPositive;
            Collections.reverse(items);
            belt.notifyUpdate();
        }

        // Assuming the first entry is furthest on the belt
        TransportedItemStack stackInFront = null;
        TransportedItemStack currentItem = null;
        Iterator<TransportedItemStack> iterator = items.iterator();

        // Useful stuff
        float beltSpeed = belt.getDirectionAwareBeltMovementSpeed();
        Direction movementFacing = belt.getMovementFacing();
        boolean horizontal = belt.getCachedState().get(BeltBlock.SLOPE) == BeltSlope.HORIZONTAL;
        float spacing = 1;
        World world = belt.getWorld();
        boolean onClient = world.isClient && !belt.isVirtual();

        // resolve ending only when items will reach it this tick
        Ending ending = Ending.UNRESOLVED;

        // Loop over items
        while (iterator.hasNext()) {
            stackInFront = currentItem;
            currentItem = iterator.next();
            currentItem.prevBeltPosition = currentItem.beltPosition;
            currentItem.prevSideOffset = currentItem.sideOffset;

            if (currentItem.stack.isEmpty()) {
                iterator.remove();
                currentItem = null;
                continue;
            }

            float movement = beltSpeed;
            if (onClient)
                movement *= AllClientHandle.INSTANCE.getServerSpeed();

            // Don't move if held by processing (client)
            if (world.isClient && currentItem.locked)
                continue;

            // Don't move if held by external components
            if (currentItem.lockedExternally) {
                currentItem.lockedExternally = false;
                continue;
            }

            // Don't move if other items are waiting in front
            boolean noMovement = false;
            float currentPos = currentItem.beltPosition;
            if (stackInFront != null) {
                float diff = stackInFront.beltPosition - currentPos;
                if (Math.abs(diff) <= spacing)
                    noMovement = true;
                movement = beltMovementPositive ? Math.min(movement, diff - spacing) : Math.max(movement, diff + spacing);
            }

            // Don't move beyond the edge
            float diffToEnd = beltMovementPositive ? belt.beltLength - currentPos : -currentPos;
            if (Math.abs(diffToEnd) < Math.abs(movement) + 1) {
                if (ending == Ending.UNRESOLVED)
                    ending = resolveEnding();
                diffToEnd += beltMovementPositive ? -ending.margin : ending.margin;
            }
            float limitedMovement = beltMovementPositive ? Math.min(movement, diffToEnd) : Math.max(movement, diffToEnd);
            float nextOffset = currentItem.beltPosition + limitedMovement;

            // Belt item processing
            if (!onClient && horizontal) {
                ItemStack item = currentItem.stack;
                if (handleBeltProcessingAndCheckIfRemoved(currentItem, nextOffset, noMovement)) {
                    iterator.remove();
                    belt.notifyUpdate();
                    continue;
                }
                if (item != currentItem.stack)
                    belt.notifyUpdate();
                if (currentItem.locked)
                    continue;
            }

            // Belt Funnels
            if (BeltFunnelInteractionHandler.checkForFunnels(this, currentItem, nextOffset))
                continue;

            if (noMovement)
                continue;

            // Belt Tunnels
            if (BeltTunnelInteractionHandler.flapTunnelsAndCheckIfStuck(this, currentItem, nextOffset))
                continue;

            // Horizontal Crushing Wheels
            if (BeltCrusherInteractionHandler.checkForCrushers(this, currentItem, nextOffset))
                continue;

            // Apply Movement
            currentItem.beltPosition += limitedMovement;
            float diffToMiddle = currentItem.getTargetSideOffset() - currentItem.sideOffset;
            currentItem.sideOffset += MathHelper.clamp(
                diffToMiddle * Math.abs(limitedMovement) * 6f,
                -Math.abs(diffToMiddle),
                Math.abs(diffToMiddle)
            );
            currentPos = currentItem.beltPosition;

            // Movement successful
            if (limitedMovement == movement || onClient)
                continue;

            // End reached
            int lastOffset = beltMovementPositive ? belt.beltLength - 1 : 0;
            BlockPos nextPosition = BeltHelper.getPositionForOffset(belt, beltMovementPositive ? belt.beltLength : -1);

            if (ending == Ending.FUNNEL)
                continue;

            if (ending == Ending.INSERT) {
                DirectBeltInputBehaviour inputBehaviour = BlockEntityBehaviour.get(world, nextPosition, DirectBeltInputBehaviour.TYPE);
                if (inputBehaviour == null)
                    continue;
                if (!inputBehaviour.canInsertFromSide(movementFacing))
                    continue;

                ItemStack remainder = inputBehaviour.handleInsertion(currentItem, movementFacing, false);
                if (ItemStack.areEqual(remainder, currentItem.stack))
                    continue;

                currentItem.stack = remainder;
                if (remainder.isEmpty()) {
                    lazyClientItem = currentItem;
                    lazyClientItem.locked = false;
                    iterator.remove();
                } else
                    currentItem.stack = remainder;

                BeltTunnelInteractionHandler.flapTunnel(this, lastOffset, movementFacing, false);
                belt.notifyUpdate();
                continue;
            }

            if (ending == Ending.BLOCKED)
                continue;

            if (ending == Ending.EJECT) {
                eject(currentItem);
                iterator.remove();
                BeltTunnelInteractionHandler.flapTunnel(this, lastOffset, movementFacing, false);
                belt.notifyUpdate();
            }
        }
    }

    protected boolean handleBeltProcessingAndCheckIfRemoved(TransportedItemStack currentItem, float nextOffset, boolean noMovement) {
        int currentSegment = (int) currentItem.beltPosition;

        // Continue processing if held
        if (currentItem.locked) {
            BeltProcessingBehaviour processingBehaviour = getBeltProcessingAtSegment(currentSegment);
            TransportedItemStackHandlerBehaviour stackHandlerBehaviour = getTransportedItemStackHandlerAtSegment(currentSegment);

            if (stackHandlerBehaviour == null)
                return false;
            if (processingBehaviour == null) {
                currentItem.locked = false;
                belt.notifyUpdate();
                return false;
            }

            ProcessingResult result = processingBehaviour.handleHeldItem(currentItem, stackHandlerBehaviour);
            if (result == ProcessingResult.REMOVE)
                return true;
            if (result == ProcessingResult.HOLD)
                return false;

            currentItem.locked = false;
            belt.notifyUpdate();
            return false;
        }

        if (noMovement)
            return false;

        // See if any new belt processing catches the item
        if (currentItem.beltPosition > .5f || beltMovementPositive) {
            int firstUpcomingSegment = (int) (currentItem.beltPosition + (beltMovementPositive ? .5f : -.5f));
            int step = beltMovementPositive ? 1 : -1;

            for (int segment = firstUpcomingSegment; beltMovementPositive ? segment + .5f <= nextOffset : segment + .5f >= nextOffset; segment += step) {

                BeltProcessingBehaviour processingBehaviour = getBeltProcessingAtSegment(segment);
                TransportedItemStackHandlerBehaviour stackHandlerBehaviour = getTransportedItemStackHandlerAtSegment(segment);

                if (processingBehaviour == null)
                    continue;
                if (stackHandlerBehaviour == null)
                    continue;
                if (BeltProcessingBehaviour.isBlocked(belt.getWorld(), BeltHelper.getPositionForOffset(belt, segment)))
                    continue;

                ProcessingResult result = processingBehaviour.handleReceivedItem(currentItem, stackHandlerBehaviour);
                if (result == ProcessingResult.REMOVE)
                    return true;

                if (result == ProcessingResult.HOLD) {
                    currentItem.beltPosition = segment + .5f + (beltMovementPositive ? 1 / 512f : -1 / 512f);
                    currentItem.locked = true;
                    belt.notifyUpdate();
                    return false;
                }
            }
        }

        return false;
    }

    protected BeltProcessingBehaviour getBeltProcessingAtSegment(int segment) {
        return BlockEntityBehaviour.get(belt.getWorld(), BeltHelper.getPositionForOffset(belt, segment).up(2), BeltProcessingBehaviour.TYPE);
    }

    protected TransportedItemStackHandlerBehaviour getTransportedItemStackHandlerAtSegment(int segment) {
        return BlockEntityBehaviour.get(belt.getWorld(), BeltHelper.getPositionForOffset(belt, segment), TransportedItemStackHandlerBehaviour.TYPE);
    }

    private enum Ending {
        UNRESOLVED(0),
        EJECT(0),
        INSERT(.25f),
        FUNNEL(.5f),
        BLOCKED(.45f);

        private float margin;

        Ending(float f) {
            this.margin = f;
        }
    }

    private Ending resolveEnding() {
        World world = belt.getWorld();
        BlockPos nextPosition = BeltHelper.getPositionForOffset(belt, beltMovementPositive ? belt.beltLength : -1);

        //		if (AllBlocks.BRASS_BELT_FUNNEL.has(world.getBlockState(lastPosition.up())))
        //			return Ending.FUNNEL;

        DirectBeltInputBehaviour inputBehaviour = BlockEntityBehaviour.get(world, nextPosition, DirectBeltInputBehaviour.TYPE);
        if (inputBehaviour != null)
            return Ending.INSERT;

        if (BlockHelper.hasBlockSolidSide(world.getBlockState(nextPosition), world, nextPosition, belt.getMovementFacing().getOpposite()))
            return Ending.BLOCKED;

        return Ending.EJECT;
    }

    //

    public boolean canInsertAt(int segment) {
        return canInsertAtFromSide(segment, Direction.UP);
    }

    public boolean canInsertAtFromSide(int segment, Direction side) {
        float segmentPos = segment;
        if (belt.getMovementFacing() == side.getOpposite())
            return false;
        if (belt.getMovementFacing() != side)
            segmentPos += .5f;
        else if (!beltMovementPositive)
            segmentPos += 1f;

        for (TransportedItemStack stack : items)
            if (isBlocking(segment, side, segmentPos, stack))
                return false;
        for (TransportedItemStack stack : toInsert)
            if (isBlocking(segment, side, segmentPos, stack))
                return false;

        return true;
    }

    private boolean isBlocking(int segment, Direction side, float segmentPos, TransportedItemStack stack) {
        float currentPos = stack.beltPosition;
        if (stack.insertedAt == segment && stack.insertedFrom == side && (beltMovementPositive ? currentPos <= segmentPos + 1 : currentPos >= segmentPos - 1))
            return true;
        return false;
    }

    public void addItem(TransportedItemStack newStack) {
        toInsert.add(newStack);
    }

    private void insert(TransportedItemStack newStack) {
        if (items.isEmpty())
            items.add(newStack);
        else {
            int index = 0;
            for (TransportedItemStack stack : items) {
                if (stack.compareTo(newStack) > 0 == beltMovementPositive)
                    break;
                index++;
            }
            items.add(index, newStack);
        }
    }

    public TransportedItemStack getStackAtOffset(int offset) {
        float min = offset;
        float max = offset + 1;
        for (TransportedItemStack stack : items) {
            if (toRemove.contains(stack))
                continue;
            if (stack.beltPosition > max)
                continue;
            if (stack.beltPosition > min)
                return stack;
        }
        return null;
    }

    public void read(ReadView view) {
        items.clear();
        ReadView.TypedListReadView<TransportedItemStack> list = view.getTypedListView("Items", TransportedItemStack.CODEC);
        list.forEach(items::add);
        lazyClientItem = view.read("LazyItem", TransportedItemStack.CODEC).orElse(null);
        beltMovementPositive = view.getBoolean("PositiveOrder", false);
    }

    public void write(WriteView view) {
        WriteView.ListAppender<TransportedItemStack> list = view.getListAppender("Items", TransportedItemStack.CODEC);
        items.forEach(list::add);
        if (lazyClientItem != null)
            view.put("LazyItem", TransportedItemStack.CODEC, lazyClientItem);
        view.putBoolean("PositiveOrder", beltMovementPositive);
    }

    public void eject(TransportedItemStack stack) {
        ItemStack ejected = stack.stack;
        Vec3d outPos = BeltHelper.getVectorForOffset(belt, stack.beltPosition);
        float movementSpeed = Math.max(Math.abs(belt.getBeltMovementSpeed()), 1 / 8f);
        Vec3d outMotion = Vec3d.of(belt.getBeltChainDirection()).multiply(movementSpeed).add(0, 1 / 8f, 0);
        outPos = outPos.add(outMotion.normalize().multiply(0.001));
        ItemEntity entity = new ItemEntity(belt.getWorld(), outPos.x, outPos.y + 6 / 16f, outPos.z, ejected);
        entity.setVelocity(outMotion);
        entity.setToDefaultPickupDelay();
        entity.velocityModified = true;
        belt.getWorld().spawnEntity(entity);
    }

    public void ejectAll() {
        items.forEach(this::eject);
        items.clear();
    }

    public void applyToEachWithin(float position, float maxDistanceToPosition, Function<TransportedItemStack, TransportedResult> processFunction) {
        boolean dirty = false;
        for (TransportedItemStack transported : items) {
            if (toRemove.contains(transported))
                continue;
            ItemStack stackBefore = transported.stack.copy();
            if (Math.abs(position - transported.beltPosition) >= maxDistanceToPosition)
                continue;
            TransportedResult result = processFunction.apply(transported);
            if (result == null || result.didntChangeFrom(stackBefore))
                continue;

            dirty = true;
            if (result.hasHeldOutput()) {
                TransportedItemStack held = result.getHeldOutput();
                held.beltPosition = ((int) position) + .5f - (beltMovementPositive ? 1 / 512f : -1 / 512f);
                toInsert.add(held);
            }
            toInsert.addAll(result.getOutputs());
            toRemove.add(transported);
        }
        if (dirty) {
            belt.notifyUpdate();
        }
    }

    public List<TransportedItemStack> getTransportedItems() {
        return items;
    }

    @Nullable
    public TransportedItemStack getLazyClientItem() {
        return lazyClientItem;
    }

}
