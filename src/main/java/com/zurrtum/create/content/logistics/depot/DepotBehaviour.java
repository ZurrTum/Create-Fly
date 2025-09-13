package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.funnel.AbstractFunnelBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DepotBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<DepotBehaviour> TYPE = new BehaviourType<>();

    public TransportedItemStack heldItem;
    public List<TransportedItemStack> incoming;
    public DepotOutputHandler processingOutputBuffer;
    public DepotItemHandler itemHandler;
    TransportedItemStackHandlerBehaviour transportedHandler;
    Supplier<Integer> maxStackSize;
    Supplier<Boolean> canAcceptItems;
    Predicate<Direction> canFunnelsPullFrom;
    Consumer<ItemStack> onHeldInserted;
    Predicate<ItemStack> acceptedItems;
    boolean allowMerge;

    public DepotBehaviour(SmartBlockEntity be) {
        super(be);
        maxStackSize = () -> heldItem != null ? heldItem.stack.getMaxCount() : 64;
        canAcceptItems = () -> true;
        canFunnelsPullFrom = $ -> true;
        acceptedItems = $ -> true;
        onHeldInserted = $ -> {
        };
        incoming = new ArrayList<>();
        itemHandler = new DepotItemHandler(this);
        processingOutputBuffer = new DepotOutputHandler();
    }

    public void enableMerging() {
        allowMerge = true;
    }

    public DepotBehaviour withCallback(Consumer<ItemStack> changeListener) {
        onHeldInserted = changeListener;
        return this;
    }

    public DepotBehaviour onlyAccepts(Predicate<ItemStack> filter) {
        acceptedItems = filter;
        return this;
    }

    @Override
    public void tick() {
        super.tick();

        World world = blockEntity.getWorld();

        for (Iterator<TransportedItemStack> iterator = incoming.iterator(); iterator.hasNext(); ) {
            TransportedItemStack ts = iterator.next();
            if (!tick(ts))
                continue;
            if (world.isClient && !blockEntity.isVirtual())
                continue;
            if (heldItem == null) {
                heldItem = ts;
            } else {
                if (!ItemHelper.canItemStackAmountsStack(heldItem.stack, ts.stack)) {
                    Vec3d vec = VecHelper.getCenterOf(blockEntity.getPos());
                    ItemScatterer.spawn(blockEntity.getWorld(), vec.x, vec.y + .5f, vec.z, ts.stack);
                } else {
                    heldItem.stack.increment(ts.stack.getCount());
                }
            }
            iterator.remove();
            blockEntity.notifyUpdate();
        }

        if (heldItem == null)
            return;
        if (!tick(heldItem))
            return;

        BlockPos pos = blockEntity.getPos();

        if (world.isClient)
            return;
        if (handleBeltFunnelOutput())
            return;

        BeltProcessingBehaviour processingBehaviour = BlockEntityBehaviour.get(world, pos.up(2), BeltProcessingBehaviour.TYPE);
        if (processingBehaviour == null)
            return;
        if (!heldItem.locked && BeltProcessingBehaviour.isBlocked(world, pos))
            return;

        ItemStack previousItem = heldItem.stack;
        boolean wasLocked = heldItem.locked;
        ProcessingResult result = wasLocked ? processingBehaviour.handleHeldItem(
            heldItem,
            transportedHandler
        ) : processingBehaviour.handleReceivedItem(heldItem, transportedHandler);
        if (result == ProcessingResult.REMOVE) {
            heldItem = null;
            blockEntity.sendData();
            return;
        }

        heldItem.locked = result == ProcessingResult.HOLD;
        if (heldItem.locked != wasLocked || !ItemStack.areEqual(previousItem, heldItem.stack))
            blockEntity.sendData();
    }

    protected boolean tick(TransportedItemStack heldItem) {
        if (heldItem.beltPosition == .5f) {
            return true;
        }
        heldItem.prevBeltPosition = heldItem.beltPosition;
        heldItem.prevSideOffset = heldItem.sideOffset;
        float diff = .5f - heldItem.beltPosition;
        if (diff > 1 / 512f) {
            if (diff > 1 / 32f && !BeltHelper.isItemUpright(heldItem.stack))
                heldItem.angle += 1;
            heldItem.beltPosition += diff / 4f;
        } else {
            heldItem.beltPosition = .5f;
        }
        return diff < 1 / 16f;
    }

    private boolean handleBeltFunnelOutput() {
        BlockState funnel = getWorld().getBlockState(getPos().up());
        Direction funnelFacing = AbstractFunnelBlock.getFunnelFacing(funnel);
        if (funnelFacing == null || !canFunnelsPullFrom.test(funnelFacing.getOpposite()))
            return false;

        for (int slot = 0; slot < processingOutputBuffer.size(); slot++) {
            ItemStack previousItem = processingOutputBuffer.getStack(slot);
            if (previousItem.isEmpty())
                continue;
            ItemStack afterInsert = blockEntity.getBehaviour(DirectBeltInputBehaviour.TYPE).tryExportingToBeltFunnel(previousItem, null, false);
            if (afterInsert == null)
                return false;
            if (previousItem.getCount() != afterInsert.getCount()) {
                processingOutputBuffer.setStack(slot, afterInsert);
                blockEntity.notifyUpdate();
                return true;
            }
        }

        ItemStack previousItem = heldItem.stack;
        ItemStack afterInsert = blockEntity.getBehaviour(DirectBeltInputBehaviour.TYPE).tryExportingToBeltFunnel(previousItem, null, false);
        if (afterInsert == null)
            return false;
        if (previousItem.getCount() != afterInsert.getCount()) {
            if (afterInsert.isEmpty())
                heldItem = null;
            else
                heldItem.stack = afterInsert;
            blockEntity.notifyUpdate();
            return true;
        }

        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        World level = getWorld();
        BlockPos pos = getPos();
        ItemScatterer.spawn(level, pos, processingOutputBuffer);
        for (TransportedItemStack transportedItemStack : incoming)
            Block.dropStack(level, pos, transportedItemStack.stack);
        if (!getHeldItemStack().isEmpty())
            Block.dropStack(level, pos, getHeldItemStack());
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        if (heldItem != null)
            view.put("HeldItem", TransportedItemStack.CODEC, heldItem);
        processingOutputBuffer.write(view);
        if (canMergeItems() && !incoming.isEmpty())
            view.put("Incoming", CreateCodecs.TRANSPORTED_ITEM_LIST_CODEC, incoming);
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        heldItem = view.read("HeldItem", TransportedItemStack.CODEC).orElse(null);
        processingOutputBuffer.read(view);
        if (canMergeItems()) {
            view.read("Incoming", CreateCodecs.TRANSPORTED_ITEM_LIST_CODEC).ifPresent(list -> incoming = list);
        }
    }

    public void addSubBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(blockEntity).allowingBeltFunnels().setInsertionHandler(this::tryInsertingFromSide)
            .considerOccupiedWhen(this::isOccupied));
        transportedHandler = new TransportedItemStackHandlerBehaviour(
            blockEntity,
            this::applyToAllItems
        ).withStackPlacement(this::getWorldPositionOf);
        behaviours.add(transportedHandler);
    }

    public ItemStack getHeldItemStack() {
        return heldItem == null ? ItemStack.EMPTY : heldItem.stack;
    }

    public boolean canMergeItems() {
        return allowMerge;
    }

    public int getPresentStackSize() {
        int cumulativeStackSize = 0;
        cumulativeStackSize += getHeldItemStack().getCount();
        for (int slot = 0; slot < processingOutputBuffer.size(); slot++)
            cumulativeStackSize += processingOutputBuffer.getStack(slot).getCount();
        return cumulativeStackSize;
    }

    public int getRemainingSpace() {
        int cumulativeStackSize = getPresentStackSize();
        for (TransportedItemStack transportedItemStack : incoming)
            cumulativeStackSize += transportedItemStack.stack.getCount();
        int fromGetter = Math.min(maxStackSize.get() == 0 ? 64 : maxStackSize.get(), getHeldItemStack().getMaxCount());
        return (fromGetter) - cumulativeStackSize;
    }

    public ItemStack insert(TransportedItemStack heldItem, boolean simulate) {
        if (!canAcceptItems.get())
            return heldItem.stack;
        if (!acceptedItems.test(heldItem.stack))
            return heldItem.stack;

        if (canMergeItems()) {
            int remainingSpace = getRemainingSpace();
            ItemStack inserted = heldItem.stack;
            if (remainingSpace <= 0)
                return inserted;
            if (this.heldItem != null && !ItemHelper.canItemStackAmountsStack(this.heldItem.stack, inserted))
                return inserted;

            ItemStack returned = ItemStack.EMPTY;
            if (remainingSpace < inserted.getCount()) {
                returned = heldItem.stack.copyWithCount(inserted.getCount() - remainingSpace);
                if (!simulate) {
                    TransportedItemStack copy = heldItem.copy();
                    copy.stack.setCount(remainingSpace);
                    if (this.heldItem != null)
                        incoming.add(copy);
                    else
                        this.heldItem = copy;
                }
            } else {
                if (!simulate) {
                    if (this.heldItem != null)
                        incoming.add(heldItem);
                    else
                        this.heldItem = heldItem;
                }
            }
            return returned;
        }

        ItemStack returned = ItemStack.EMPTY;
        int maxCount = heldItem.stack.getMaxCount();
        boolean stackTooLarge = maxCount < heldItem.stack.getCount();
        if (stackTooLarge)
            returned = heldItem.stack.copyWithCount(heldItem.stack.getCount() - maxCount);

        if (simulate)
            return returned;

        if (this.isEmpty()) {
            if (heldItem.insertedFrom.getAxis().isHorizontal())
                AllSoundEvents.DEPOT_SLIDE.playOnServer(getWorld(), getPos());
            else
                AllSoundEvents.DEPOT_PLOP.playOnServer(getWorld(), getPos());
        }

        if (stackTooLarge) {
            heldItem = heldItem.copy();
            heldItem.stack.setCount(maxCount);
        }

        this.heldItem = heldItem;
        onHeldInserted.accept(heldItem.stack);
        return returned;
    }

    public void setHeldItem(TransportedItemStack heldItem) {
        this.heldItem = heldItem;
    }

    public void removeHeldItem() {
        this.heldItem = null;
    }

    public void setCenteredHeldItem(TransportedItemStack heldItem) {
        this.heldItem = heldItem;
        this.heldItem.beltPosition = 0.5f;
        this.heldItem.prevBeltPosition = 0.5f;
    }

    private boolean isOccupied(Direction side) {
        if (!getHeldItemStack().isEmpty() && !canMergeItems())
            return true;
        if (!isOutputEmpty() && !canMergeItems())
            return true;
        if (!canAcceptItems.get())
            return true;
        return false;
    }

    private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
        ItemStack inserted = transportedStack.stack;

        if (isOccupied(side))
            return inserted;

        int size = transportedStack.stack.getCount();
        transportedStack = transportedStack.copy();
        transportedStack.beltPosition = side.getAxis().isVertical() ? .5f : 0;
        transportedStack.insertedFrom = side;
        transportedStack.prevSideOffset = transportedStack.sideOffset;
        transportedStack.prevBeltPosition = transportedStack.beltPosition;
        ItemStack remainder = insert(transportedStack, simulate);
        if (remainder.getCount() != size)
            blockEntity.notifyUpdate();

        return remainder;
    }

    private void applyToAllItems(float maxDistanceFromCentre, Function<TransportedItemStack, TransportedResult> processFunction) {
        if (heldItem == null)
            return;
        if (.5f - heldItem.beltPosition > maxDistanceFromCentre)
            return;

        TransportedItemStack transportedItemStack = heldItem;
        ItemStack stackBefore = transportedItemStack.stack.copy();
        TransportedResult result = processFunction.apply(transportedItemStack);
        if (result == null || result.didntChangeFrom(stackBefore))
            return;

        heldItem = null;
        if (result.hasHeldOutput())
            setCenteredHeldItem(result.getHeldOutput());

        List<TransportedItemStack> outputs = result.getOutputs();
        int skip = 0;
        if (getHeldItemStack().isEmpty()) {
            setCenteredHeldItem(outputs.getFirst());
            skip = 1;
        }
        List<ItemStack> items = outputs.stream().skip(skip).map(t -> t.stack).toList();
        items = processingOutputBuffer.insert(items);
        if (!items.isEmpty()) {
            World world = blockEntity.getWorld();
            Vec3d vec = VecHelper.getCenterOf(blockEntity.getPos()).add(0, .5f, 0);
            double x = vec.x;
            double y = vec.y + .5f;
            double z = vec.z;
            for (ItemStack stack : items) {
                ItemScatterer.spawn(world, x, y, z, stack);
            }
        }

        blockEntity.notifyUpdate();
    }

    public boolean isEmpty() {
        return heldItem == null && isOutputEmpty();
    }

    public boolean isOutputEmpty() {
        return processingOutputBuffer.isEmpty();
    }

    private Vec3d getWorldPositionOf(TransportedItemStack transported) {
        return VecHelper.getCenterOf(blockEntity.getPos());
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public boolean isItemValid(ItemStack stack) {
        return acceptedItems.test(stack);
    }

    public class DepotOutputHandler implements ItemInventory {
        private final DefaultedList<ItemStack> stacks;

        public DepotOutputHandler() {
            this.stacks = DefaultedList.ofSize(8, ItemStack.EMPTY);
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot >= size()) {
                return ItemStack.EMPTY;
            }
            return stacks.get(slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            stacks.set(slot, stack);
        }

        @Override
        public void markDirty() {
            blockEntity.notifyUpdate();
        }

        public void write(WriteView view) {
            WriteView.ListAppender<ItemStack> list = view.getListAppender("Inventory", ItemStack.CODEC);
            for (ItemStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                list.add(stack);
            }
        }

        public void read(ReadView view) {
            ReadView.TypedListReadView<ItemStack> list = view.getTypedListView("Inventory", ItemStack.CODEC);
            int i = 0;
            for (ItemStack itemStack : list) {
                stacks.set(i++, itemStack);
            }
            for (int size = stacks.size(); i < size; i++) {
                stacks.set(i, ItemStack.EMPTY);
            }
        }
    }
}
