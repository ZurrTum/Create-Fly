package com.zurrtum.create.content.processing.basin;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BasinBlockEntity extends SmartBlockEntity {
    public boolean areFluidsMoving;
    LerpedFloat ingredientRotationSpeed;
    public LerpedFloat ingredientRotation;

    public SmartFluidTankBehaviour inputTank;
    protected SmartFluidTankBehaviour outputTank;
    private ServerFilteringBehaviour filtering;
    private boolean contentsChanged;

    private Couple<SmartFluidTankBehaviour> tanks;

    public BasinInventory itemCapability;
    public BasinFluidHandler fluidCapability;

    List<Direction> disabledSpoutputs;
    Direction preferredSpoutput;
    protected List<ItemStack> spoutputBuffer;
    protected List<FluidStack> spoutputFluidBuffer;
    int recipeBackupCheck;

    public static final int OUTPUT_ANIMATION_TIME = 10;
    public List<IntAttached<ItemStack>> visualizedOutputItems;
    public List<IntAttached<FluidStack>> visualizedOutputFluids;

    private @Nullable HeatLevel cachedHeatLevel;

    public BasinBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.BASIN, pos, state);
        areFluidsMoving = false;
        itemCapability = new BasinInventory(this);
        contentsChanged = true;
        ingredientRotation = LerpedFloat.angular().startWithValue(0);
        ingredientRotationSpeed = LerpedFloat.linear().startWithValue(0);

        tanks = Couple.create(inputTank, outputTank);
        visualizedOutputItems = Collections.synchronizedList(new ArrayList<>());
        visualizedOutputFluids = Collections.synchronizedList(new ArrayList<>());
        disabledSpoutputs = new ArrayList<>();
        preferredSpoutput = null;
        spoutputBuffer = new ArrayList<>();
        spoutputFluidBuffer = new ArrayList<>();
        recipeBackupCheck = 20;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this));
        filtering = new ServerFilteringBehaviour(this).withCallback(newFilter -> contentsChanged = true).forRecipes();
        behaviours.add(filtering);

        inputTank = new SmartFluidTankBehaviour(
            SmartFluidTankBehaviour.INPUT,
            this,
            2,
            BucketFluidInventory.CAPACITY,
            true
        ).whenFluidUpdates(() -> contentsChanged = true);
        outputTank = new SmartFluidTankBehaviour(
            SmartFluidTankBehaviour.OUTPUT,
            this,
            2,
            BucketFluidInventory.CAPACITY,
            true
        ).whenFluidUpdates(() -> contentsChanged = true).forbidInsertion();
        behaviours.add(inputTank);
        behaviours.add(outputTank);

        fluidCapability = new BasinFluidHandler(outputTank.getTanks(), inputTank.getTanks());
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        itemCapability.read(view);

        preferredSpoutput = view.read("PreferredSpoutput", Direction.CODEC).orElse(null);
        disabledSpoutputs.clear();
        spoutputBuffer.clear();
        spoutputFluidBuffer.clear();
        view.read("DisabledSpoutput", CreateCodecs.DIRECTION_LIST_CODEC).ifPresent(disabledSpoutputs::addAll);
        view.read("Overflow", CreateCodecs.ITEM_LIST_CODEC).ifPresent(spoutputBuffer::addAll);
        view.read("FluidOverflow", CreateCodecs.FLUID_LIST_CODEC).ifPresent(spoutputFluidBuffer::addAll);

        if (!clientPacket)
            return;

        view.getTypedListView("VisualizedItems", ItemStack.OPTIONAL_CODEC).stream()
            .forEach(stack -> visualizedOutputItems.add(IntAttached.with(OUTPUT_ANIMATION_TIME, stack)));
        view.getTypedListView("VisualizedFluids", FluidStack.OPTIONAL_CODEC).stream()
            .forEach(stack -> visualizedOutputFluids.add(IntAttached.with(OUTPUT_ANIMATION_TIME, stack)));
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        itemCapability.write(view);

        if (preferredSpoutput != null)
            view.put("PreferredSpoutput", Direction.CODEC, preferredSpoutput);
        view.put("DisabledSpoutput", CreateCodecs.DIRECTION_LIST_CODEC, disabledSpoutputs);
        view.put("Overflow", CreateCodecs.ITEM_LIST_CODEC, spoutputBuffer);
        view.put("FluidOverflow", CreateCodecs.FLUID_LIST_CODEC, spoutputFluidBuffer);

        if (!clientPacket)
            return;

        WriteView.ListAppender<ItemStack> items = view.getListAppender("VisualizedItems", ItemStack.OPTIONAL_CODEC);
        visualizedOutputItems.stream().map(IntAttached::getValue).forEach(items::add);
        WriteView.ListAppender<FluidStack> fluids = view.getListAppender("VisualizedFluids", FluidStack.OPTIONAL_CODEC);
        visualizedOutputFluids.stream().map(IntAttached::getValue).forEach(fluids::add);
        visualizedOutputItems.clear();
        visualizedOutputFluids.clear();
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemScatterer.spawn(world, pos, itemCapability);
        spoutputBuffer.forEach(is -> Block.dropStack(world, pos, is));
    }

    @Override
    public void remove() {
        super.remove();
        onEmptied();
    }

    public void onEmptied() {
        getOperator().ifPresent(be -> be.basinRemoved = true);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();

        if (!world.isClient) {
            updateSpoutput();
            if (recipeBackupCheck-- > 0)
                return;
            recipeBackupCheck = 20;
            if (isEmpty())
                return;
            notifyChangeOfContents();
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos.up(2));
        if (!(blockEntity instanceof MechanicalMixerBlockEntity mixer)) {
            setAreFluidsMoving(false);
            return;
        }

        //        setAreFluidsMoving(mixer.running && mixer.runningTicks <= 20);
    }

    public boolean isEmpty() {
        return itemCapability.isEmpty() && inputTank.isEmpty() && outputTank.isEmpty();
    }

    public void onWrenched(Direction face) {
        BlockState blockState = getCachedState();
        Direction currentFacing = blockState.get(BasinBlock.FACING);

        disabledSpoutputs.remove(face);
        if (currentFacing == face) {
            if (preferredSpoutput == face)
                preferredSpoutput = null;
            disabledSpoutputs.add(face);
        } else
            preferredSpoutput = face;

        updateSpoutput();
    }

    private void updateSpoutput() {
        BlockState blockState = getCachedState();
        Direction currentFacing = blockState.get(BasinBlock.FACING);
        Direction newFacing = Direction.DOWN;
        for (Direction test : Iterate.horizontalDirections) {
            boolean canOutputTo = BasinBlock.canOutputTo(world, pos, test);
            if (canOutputTo && !disabledSpoutputs.contains(test))
                newFacing = test;
        }

        if (preferredSpoutput != null && BasinBlock.canOutputTo(world, pos, preferredSpoutput) && preferredSpoutput != Direction.UP)
            newFacing = preferredSpoutput;

        if (newFacing == currentFacing)
            return;

        world.setBlockState(pos, blockState.with(BasinBlock.FACING, newFacing));

        if (newFacing.getAxis().isVertical())
            return;

        for (int slot = 9; slot < 18; slot++) {
            ItemStack stack = itemCapability.getStack(slot);
            if (stack.isEmpty())
                continue;
            if (acceptOutputs(ImmutableList.of(stack), Collections.emptyList(), true)) {
                acceptOutputs(ImmutableList.of(stack), Collections.emptyList(), false);
                itemCapability.setStack(slot, ItemStack.EMPTY);
            }
        }

        FluidInventory handler = outputTank.getCapability();
        for (int slot = 0; slot < 2; slot++) {
            FluidStack fs = handler.getStack(slot);
            if (fs.isEmpty())
                continue;
            fs = fs.copy();
            if (acceptOutputs(Collections.emptyList(), ImmutableList.of(fs), true)) {
                handler.setStack(slot, FluidStack.EMPTY);
                acceptOutputs(Collections.emptyList(), ImmutableList.of(fs), false);
            }
        }

        notifyChangeOfContents();
        notifyUpdate();
    }

    @Override
    public void tick() {
        cachedHeatLevel = null;

        super.tick();
        if (world.isClient) {
            AllClientHandle.INSTANCE.createBasinFluidParticles(world, this);
            tickVisualizedOutputs();
            ingredientRotationSpeed.tickChaser();
            ingredientRotation.setValue(ingredientRotation.getValue() + ingredientRotationSpeed.getValue());
        }

        if ((!spoutputBuffer.isEmpty() || !spoutputFluidBuffer.isEmpty()) && !world.isClient)
            tryClearingSpoutputOverflow();
        if (!contentsChanged)
            return;

        contentsChanged = false;
        getOperator().ifPresent(be -> be.basinChecker.scheduleUpdate());

        for (Direction offset : Iterate.horizontalDirections) {
            BlockPos toUpdate = pos.up().offset(offset);
            BlockState stateToUpdate = world.getBlockState(toUpdate);
            if (stateToUpdate.getBlock() instanceof BasinBlock && stateToUpdate.get(BasinBlock.FACING) == offset.getOpposite()) {
                BlockEntity be = world.getBlockEntity(toUpdate);
                if (be instanceof BasinBlockEntity)
                    ((BasinBlockEntity) be).contentsChanged = true;
            }
        }
    }

    private void tryClearingSpoutputOverflow() {
        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof BasinBlock))
            return;
        Direction direction = blockState.get(BasinBlock.FACING);
        BlockEntity be = world.getBlockEntity(pos.down().offset(direction));

        ServerFilteringBehaviour filter = null;
        InvManipulationBehaviour inserter = null;
        if (be != null) {
            filter = BlockEntityBehaviour.get(world, be.getPos(), ServerFilteringBehaviour.TYPE);
            inserter = BlockEntityBehaviour.get(world, be.getPos(), InvManipulationBehaviour.TYPE);
        }

        if (filter != null && filter.isRecipeFilter())
            filter = null; // Do not test spout outputs against the recipe filter

        Direction opposite = direction.getOpposite();
        Inventory targetInv = be == null ? null : ItemHelper.getInventory(world, be.getPos(), null, be, opposite);
        if (targetInv == null && inserter != null) {
            targetInv = inserter.getInventory();
        }

        FluidInventory targetTank = be == null ? null : FluidHelper.getFluidInventory(world, be.getPos(), null, be, opposite);

        boolean update = false;

        for (Iterator<ItemStack> iterator = spoutputBuffer.iterator(); iterator.hasNext(); ) {
            ItemStack itemStack = iterator.next();

            if (direction == Direction.DOWN) {
                Block.dropStack(world, pos, itemStack);
                iterator.remove();
                update = true;
                continue;
            }

            if (targetInv == null)
                break;

            if (targetInv.countSpace(itemStack, 1) == 0)
                continue;
            if (filter != null && !filter.test(itemStack))
                continue;

            if (visualizedOutputItems.size() < 3)
                visualizedOutputItems.add(IntAttached.withZero(itemStack));
            update = true;

            int count = itemStack.getCount();
            int insert = targetInv.insertExist(itemStack, opposite);
            if (insert == count)
                iterator.remove();
            else
                itemStack.decrement(insert);
        }

        for (Iterator<FluidStack> iterator = spoutputFluidBuffer.iterator(); iterator.hasNext(); ) {
            FluidStack fluidStack = iterator.next();

            if (direction == Direction.DOWN) {
                iterator.remove();
                update = true;
                continue;
            }

            if (targetTank == null)
                break;

            if (targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler) {
                if (!targetTank.forcePreciseInsert(fluidStack)) {
                    continue;
                }
            } else if (!targetTank.preciseInsert(fluidStack, opposite)) {
                continue;
            }
            update = true;
            iterator.remove();
            if (visualizedOutputFluids.size() < 3)
                visualizedOutputFluids.add(IntAttached.withZero(fluidStack));
        }

        if (update) {
            notifyChangeOfContents();
            sendData();
        }
    }

    public float getTotalFluidUnits(float partialTicks) {
        int renderedFluids = 0;
        float totalUnits = 0;

        for (SmartFluidTankBehaviour behaviour : getTanks()) {
            if (behaviour == null)
                continue;
            for (TankSegment tankSegment : behaviour.getTanks()) {
                if (tankSegment.getRenderedFluid().isEmpty())
                    continue;
                float units = tankSegment.getTotalUnits(partialTicks);
                if (units < 1)
                    continue;
                totalUnits += units;
                renderedFluids++;
            }
        }

        if (renderedFluids == 0)
            return 0;
        if (totalUnits < 1)
            return 0;
        return totalUnits;
    }

    private Optional<BasinOperatingBlockEntity> getOperator() {
        if (world == null)
            return Optional.empty();
        BlockEntity be = world.getBlockEntity(pos.up(2));
        if (be instanceof BasinOperatingBlockEntity)
            return Optional.of((BasinOperatingBlockEntity) be);
        return Optional.empty();
    }

    public ServerFilteringBehaviour getFilter() {
        return filtering;
    }

    public void notifyChangeOfContents() {
        contentsChanged = true;
    }

    public boolean canContinueProcessing() {
        return spoutputBuffer.isEmpty() && spoutputFluidBuffer.isEmpty();
    }

    public boolean acceptOutputs(List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean simulate) {
        itemCapability.disableCheck();
        outputTank.allowInsertion();
        boolean acceptOutputsInner = acceptOutputsInner(outputItems, outputFluids, simulate);
        itemCapability.enableCheck();
        outputTank.forbidInsertion();
        return acceptOutputsInner;
    }

    private boolean acceptOutputsInner(List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean simulate) {
        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof BasinBlock))
            return false;

        Direction direction = blockState.get(BasinBlock.FACING);
        if (direction != Direction.DOWN) {

            BlockEntity be = world.getBlockEntity(pos.down().offset(direction));

            InvManipulationBehaviour inserter = be == null ? null : BlockEntityBehaviour.get(world, be.getPos(), InvManipulationBehaviour.TYPE);
            Direction opposite = direction.getOpposite();
            Inventory targetInv = be == null ? null : ItemHelper.getInventory(world, be.getPos(), null, be, opposite);
            if (targetInv == null && inserter != null) {
                targetInv = inserter.getInventory();
            }
            FluidInventory targetTank = be == null ? null : FluidHelper.getFluidInventory(world, be.getPos(), null, be, opposite);
            boolean externalTankNotPresent = targetTank == null;

            if (!outputItems.isEmpty() && targetInv == null)
                return false;
            if (!outputFluids.isEmpty() && externalTankNotPresent) {
                // Special case - fluid outputs but output only accepts items
                targetTank = outputTank.getCapability();
                if (targetTank == null)
                    return false;
                if (!acceptFluidOutputsIntoBasin(outputFluids, simulate, targetTank))
                    return false;
            }

            if (simulate)
                return true;
            for (ItemStack itemStack : outputItems)
                if (!itemStack.isEmpty())
                    spoutputBuffer.add(itemStack.copy());
            if (!externalTankNotPresent)
                for (FluidStack fluidStack : outputFluids)
                    spoutputFluidBuffer.add(fluidStack.copy());
            return true;
        }

        if (!acceptItemOutputsIntoBasin(outputItems, simulate, itemCapability))
            return false;
        if (outputFluids.isEmpty())
            return true;
        return acceptFluidOutputsIntoBasin(outputFluids, simulate, outputTank.getCapability());
    }

    private boolean acceptFluidOutputsIntoBasin(List<FluidStack> outputFluids, boolean simulate, FluidInventory targetTank) {
        if (simulate) {
            return targetTank.countSpace(outputFluids);
        } else {
            targetTank.insert(outputFluids);
            return true;
        }
    }

    private boolean acceptItemOutputsIntoBasin(List<ItemStack> outputItems, boolean simulate, Inventory targetInv) {
        if (simulate) {
            return targetInv.countSpace(outputItems, 9, 17);
        } else {
            targetInv.insert(outputItems, 9, 17);
            return true;
        }
    }

    public void readOnlyItems(ReadView view) {
        itemCapability.read(view);
    }

    public static HeatLevel getHeatLevelOf(BlockState state) {
        if (state.contains(BlazeBurnerBlock.HEAT_LEVEL))
            return state.get(BlazeBurnerBlock.HEAT_LEVEL);
        return state.isIn(AllBlockTags.PASSIVE_BOILER_HEATERS) && BlockHelper.isNotUnheated(state) ? HeatLevel.SMOULDERING : HeatLevel.NONE;
    }

    public Couple<SmartFluidTankBehaviour> getTanks() {
        return tanks;
    }

    // client things

    private void tickVisualizedOutputs() {
        visualizedOutputFluids.forEach(IntAttached::decrement);
        visualizedOutputItems.forEach(IntAttached::decrement);
        visualizedOutputFluids.removeIf(IntAttached::isOrBelowZero);
        visualizedOutputItems.removeIf(IntAttached::isOrBelowZero);
    }

    public boolean setAreFluidsMoving(boolean areFluidsMoving) {
        this.areFluidsMoving = areFluidsMoving;
        ingredientRotationSpeed.chase(areFluidsMoving ? 20 : 0, .1f, Chaser.EXP);
        return areFluidsMoving;
    }

    public static class BasinFluidHandler implements FluidInventory {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private static final Optional<Integer> LIMIT = Optional.of(BucketFluidInventory.CAPACITY);
        private final TankSegment[] output;
        private final TankSegment[] input;

        public BasinFluidHandler(TankSegment[] output, TankSegment[] input) {
            this.output = output;
            this.input = input;
        }

        @Override
        public int getMaxAmountPerStack() {
            return BucketFluidInventory.CAPACITY;
        }

        @Override
        public FluidStack onExtract(FluidStack stack) {
            return removeMaxSize(stack, LIMIT);
        }

        @Override
        public boolean isValid(int slot, FluidStack stack) {
            if (slot < 2) {
                return false;
            }
            for (int i = 0, size = input.length, current = slot - 2; i < size; i++) {
                FluidStack fluid = input[i].getFluid();
                if (fluid.isEmpty()) {
                    continue;
                }
                if (matches(fluid, stack) && i != current) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int size() {
            return 4;
        }

        @Override
        public FluidStack getStack(int slot) {
            if (slot >= 4) {
                return FluidStack.EMPTY;
            }
            return slot < 2 ? output[slot].getFluid() : input[slot - 2].getFluid();
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            if (slot >= 4) {
                return;
            }
            TankSegment tank;
            if (slot < 2) {
                tank = output[slot];
            } else {
                tank = input[slot - 2];
            }
            tank.setFluid(stack);
        }

        @Override
        public void markDirty() {
            for (TankSegment tank : input) {
                tank.markDirty();
            }
            for (TankSegment tank : output) {
                tank.markDirty();
            }
        }
    }

    @NotNull HeatLevel getHeatLevel() {
        if (cachedHeatLevel == null) {
            if (world == null)
                return HeatLevel.NONE;

            cachedHeatLevel = getHeatLevelOf(world.getBlockState(getPos().down(1)));
        }
        return cachedHeatLevel;
    }
}
