package com.zurrtum.create.content.fluids.drain;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemDrainBlockEntity extends SmartBlockEntity {

    public static final int FILLING_TIME = 20;

    public SmartFluidTankBehaviour internalTank;
    public TransportedItemStack heldItem;
    public int processingTicks;
    public Map<Direction, ItemDrainItemHandler> itemHandlers;

    public ItemDrainBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ITEM_DRAIN, pos, state);
        itemHandlers = new IdentityHashMap<>();
        for (Direction d : Iterate.horizontalDirections) {
            itemHandlers.put(d, new ItemDrainItemHandler(this, d));
        }
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        ItemStack heldItemStack = getHeldItemStack();
        if (!heldItemStack.isEmpty())
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), heldItemStack);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnels().setInsertionHandler(this::tryInsertingFromSide));
        behaviours.add(internalTank = SmartFluidTankBehaviour.single(this, (int) (1.5 * BucketFluidInventory.CAPACITY), ItemDrainFluidHandler::new)
            .allowExtraction().forbidInsertion());
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.DRAIN, AllAdvancements.CHAINED_DRAIN);
    }

    private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
        ItemStack inserted = transportedStack.stack;
        ItemStack returned = ItemStack.EMPTY;

        if (!getHeldItemStack().isEmpty())
            return inserted;

        if (inserted.getCount() > 1 && GenericItemEmptying.canItemBeEmptied(world, inserted)) {
            returned = inserted.copyWithCount(inserted.getCount() - 1);
            inserted = inserted.copyWithCount(1);
        }

        if (simulate)
            return returned;

        transportedStack = transportedStack.copy();
        transportedStack.stack = inserted.copy();
        transportedStack.beltPosition = side.getAxis().isVertical() ? .5f : 0;
        transportedStack.prevSideOffset = transportedStack.sideOffset;
        transportedStack.prevBeltPosition = transportedStack.beltPosition;
        setHeldItem(transportedStack, side);
        markDirty();
        sendData();

        return returned;
    }

    public ItemStack getHeldItemStack() {
        return heldItem == null ? ItemStack.EMPTY : heldItem.stack;
    }

    @Override
    public void tick() {
        super.tick();

        if (heldItem == null) {
            processingTicks = 0;
            return;
        }

        boolean onClient = world.isClient() && !isVirtual();

        if (processingTicks > 0) {
            heldItem.prevBeltPosition = .5f;
            boolean wasAtBeginning = processingTicks == FILLING_TIME;
            if (!onClient || processingTicks < FILLING_TIME)
                processingTicks--;
            if (!continueProcessing()) {
                processingTicks = 0;
                notifyUpdate();
                return;
            }
            if (wasAtBeginning != (processingTicks == FILLING_TIME))
                sendData();
            return;
        }

        heldItem.prevBeltPosition = heldItem.beltPosition;
        heldItem.prevSideOffset = heldItem.sideOffset;

        heldItem.beltPosition += itemMovementPerTick();
        if (heldItem.beltPosition > 1) {
            heldItem.beltPosition = 1;

            if (onClient)
                return;

            Direction side = heldItem.insertedFrom;

            ItemStack tryExportingToBeltFunnel = getBehaviour(DirectBeltInputBehaviour.TYPE).tryExportingToBeltFunnel(
                heldItem.stack,
                side.getOpposite(),
                false
            );
            if (tryExportingToBeltFunnel != null) {
                if (tryExportingToBeltFunnel.getCount() != heldItem.stack.getCount()) {
                    if (tryExportingToBeltFunnel.isEmpty())
                        heldItem = null;
                    else
                        heldItem.stack = tryExportingToBeltFunnel;
                    notifyUpdate();
                    return;
                }
                if (!tryExportingToBeltFunnel.isEmpty())
                    return;
            }

            BlockPos nextPosition = pos.offset(side);
            DirectBeltInputBehaviour directBeltInputBehaviour = BlockEntityBehaviour.get(world, nextPosition, DirectBeltInputBehaviour.TYPE);
            if (directBeltInputBehaviour == null) {
                if (!BlockHelper.hasBlockSolidSide(world.getBlockState(nextPosition), world, nextPosition, side.getOpposite())) {
                    ItemStack ejected = heldItem.stack;
                    Vec3d outPos = VecHelper.getCenterOf(pos).add(Vec3d.of(side.getVector()).multiply(.75));
                    float movementSpeed = itemMovementPerTick();
                    Vec3d outMotion = Vec3d.of(side.getVector()).multiply(movementSpeed).add(0, 1 / 8f, 0);
                    outPos.add(outMotion.normalize());
                    ItemEntity entity = new ItemEntity(world, outPos.x, outPos.y + 6 / 16f, outPos.z, ejected);
                    entity.setVelocity(outMotion);
                    entity.setToDefaultPickupDelay();
                    entity.velocityModified = true;
                    world.spawnEntity(entity);

                    heldItem = null;
                    notifyUpdate();
                }
                return;
            }

            if (!directBeltInputBehaviour.canInsertFromSide(side))
                return;

            ItemStack returned = directBeltInputBehaviour.handleInsertion(heldItem.copy(), side, false);

            if (returned.isEmpty()) {
                if (world.getBlockEntity(nextPosition) instanceof ItemDrainBlockEntity)
                    award(AllAdvancements.CHAINED_DRAIN);
                heldItem = null;
                notifyUpdate();
                return;
            }

            if (returned.getCount() != heldItem.stack.getCount()) {
                heldItem.stack = returned;
                notifyUpdate();
                return;
            }

            return;
        }

        if (heldItem.prevBeltPosition < .5f && heldItem.beltPosition >= .5f) {
            if (!GenericItemEmptying.canItemBeEmptied(world, heldItem.stack))
                return;
            heldItem.beltPosition = .5f;
            if (onClient)
                return;
            processingTicks = FILLING_TIME;
            sendData();
        }

    }

    protected boolean continueProcessing() {
        if (world.isClient() && !isVirtual())
            return true;
        if (processingTicks < 5)
            return true;
        if (!GenericItemEmptying.canItemBeEmptied(world, heldItem.stack))
            return false;

        Pair<FluidStack, ItemStack> emptyItem = GenericItemEmptying.emptyItem(world, heldItem.stack, true);
        FluidStack fluidFromItem = emptyItem.getFirst();

        if (processingTicks > 5) {
            internalTank.allowInsertion();
            int amount = fluidFromItem.getAmount();
            if (internalTank.getPrimaryHandler().countSpace(fluidFromItem) != amount) {
                internalTank.forbidInsertion();
                processingTicks = FILLING_TIME;
                return true;
            }
            internalTank.forbidInsertion();
            return true;
        }

        emptyItem = GenericItemEmptying.emptyItem(world, heldItem.stack.copy(), false);
        award(AllAdvancements.DRAIN);

        // Process finished
        ItemStack out = emptyItem.getSecond();
        if (!out.isEmpty())
            heldItem.stack = out;
        else
            heldItem = null;
        internalTank.allowInsertion();
        internalTank.getPrimaryHandler().insert(fluidFromItem);
        internalTank.forbidInsertion();
        notifyUpdate();
        return true;
    }

    private float itemMovementPerTick() {
        return 1 / 8f;
    }

    public void setHeldItem(TransportedItemStack heldItem, Direction insertedFrom) {
        this.heldItem = heldItem;
        this.heldItem.insertedFrom = insertedFrom;
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("ProcessingTicks", processingTicks);
        if (heldItem != null)
            view.put("HeldItem", TransportedItemStack.CODEC, heldItem);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        heldItem = null;
        processingTicks = view.getInt("ProcessingTicks", 0);
        heldItem = view.read("HeldItem", TransportedItemStack.CODEC).orElse(null);
        super.read(view, clientPacket);
    }

    public static class ItemDrainFluidHandler extends SmartFluidTankBehaviour.InternalFluidHandler {
        private static final int[] EMPTY_SLOTS = new int[0];

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public ItemDrainFluidHandler(SmartFluidTankBehaviour behaviour, boolean enforceVariety, Optional<Integer> max) {
            super(behaviour, enforceVariety, max);
        }

        @Override
        public int[] getAvailableSlots(@Nullable Direction side) {
            if (side == Direction.UP) {
                return EMPTY_SLOTS;
            }
            return super.getAvailableSlots(side);
        }
    }
}
