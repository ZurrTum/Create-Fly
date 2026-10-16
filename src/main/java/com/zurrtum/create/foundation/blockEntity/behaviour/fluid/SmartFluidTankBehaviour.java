package com.zurrtum.create.foundation.blockEntity.behaviour.fluid;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.function.TriFunction;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class SmartFluidTankBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<SmartFluidTankBehaviour>

        TYPE = new BehaviourType<>(), INPUT = new BehaviourType<>("Input"), OUTPUT = new BehaviourType<>("Output");

    private static final int SYNC_RATE = 8;

    protected int syncCooldown;
    protected boolean queuedSync;
    protected final TankSegment[] tanks;
    protected SidedFluidInventory capability;
    protected boolean extractionAllowed;
    protected boolean insertionAllowed;
    protected Runnable fluidUpdateCallback;

    private final BehaviourType<SmartFluidTankBehaviour> behaviourType;

    public static SmartFluidTankBehaviour single(SmartBlockEntity be, int capacity) {
        return new SmartFluidTankBehaviour(TYPE, be, 1, capacity, false);
    }

    public static SmartFluidTankBehaviour single(
        SmartBlockEntity be,
        int capacity,
        TriFunction<SmartFluidTankBehaviour, Boolean, Integer, InternalFluidHandler> factory
    ) {
        return new SmartFluidTankBehaviour(TYPE, be, 1, capacity, false, factory);
    }

    public SmartFluidTankBehaviour(
        BehaviourType<SmartFluidTankBehaviour> type,
        SmartBlockEntity be,
        int tanks,
        int tankCapacity,
        boolean enforceVariety,
        TriFunction<SmartFluidTankBehaviour, Boolean, Integer, InternalFluidHandler> factory
    ) {
        super(be);
        insertionAllowed = true;
        extractionAllowed = true;
        behaviourType = type;
        this.tanks = new TankSegment[tanks];
        for (int i = 0; i < tanks; i++) {
            this.tanks[i] = new TankSegment(tankCapacity);
        }
        capability = factory.apply(this, enforceVariety, tankCapacity);
        fluidUpdateCallback = () -> {
        };
    }

    public SmartFluidTankBehaviour(
        BehaviourType<SmartFluidTankBehaviour> type,
        SmartBlockEntity be,
        int tanks,
        int tankCapacity,
        boolean enforceVariety
    ) {
        this(type, be, tanks, tankCapacity, enforceVariety, InternalFluidHandler::new);
    }

    public SmartFluidTankBehaviour whenFluidUpdates(Runnable fluidUpdateCallback) {
        this.fluidUpdateCallback = fluidUpdateCallback;
        return this;
    }

    public SmartFluidTankBehaviour allowInsertion() {
        insertionAllowed = true;
        return this;
    }

    public SmartFluidTankBehaviour allowExtraction() {
        extractionAllowed = true;
        return this;
    }

    public SmartFluidTankBehaviour forbidInsertion() {
        insertionAllowed = false;
        return this;
    }

    public SmartFluidTankBehaviour forbidExtraction() {
        extractionAllowed = false;
        return this;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (getLevel().isClientSide()) {
            return;
        }
        forEach(ts -> {
            ts.fluidLevel.forceNextSync();
            ts.markDirty();
        });
    }

    @Override
    public void tick() {
        super.tick();

        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync) {
                updateFluids();
            }
        }

        forEach(be -> {
            LerpedFloat fluidLevel = be.getFluidLevel();
            if (fluidLevel != null) {
                fluidLevel.tickChaser();
            }
        });
    }

    public void sendDataImmediately() {
        syncCooldown = 0;
        queuedSync = false;
        updateFluids();
    }

    public void sendDataLazily() {
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        updateFluids();
        queuedSync = false;
        syncCooldown = SYNC_RATE;
    }

    protected void updateFluids() {
        fluidUpdateCallback.run();
        blockEntity.sendData();
        blockEntity.setChanged();
    }

    public TankSegment getPrimaryHandler() {
        return getPrimaryTank();
    }

    public TankSegment getPrimaryTank() {
        return tanks[0];
    }

    public TankSegment[] getTanks() {
        return tanks;
    }

    public boolean isEmpty() {
        for (TankSegment tankSegment : tanks) {
            if (!tankSegment.getFluid().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void forEach(Consumer<TankSegment> action) {
        for (TankSegment tankSegment : tanks) {
            action.accept(tankSegment);
        }
    }

    public SidedFluidInventory getCapability() {
        return capability;
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        ValueOutput.ValueOutputList list = view.childrenList(getType().getName() + "Tanks");
        forEach(ts -> ts.write(list.addChild()));
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        int i = 0;
        int size = tanks.length;
        for (ValueInput item : view.childrenListOrEmpty(getType().getName() + "Tanks")) {
            if (i >= size) {
                break;
            }
            tanks[i].read(item, clientPacket);
            i++;
        }
    }

    public static class InternalFluidHandler implements SidedFluidInventory {
        private final boolean enforceVariety;
        private final int[] slots;
        private final SmartFluidTankBehaviour behaviour;
        private final TankSegment[] tanks;
        private final int capacity;

        public InternalFluidHandler(SmartFluidTankBehaviour behaviour, boolean enforceVariety, int capacity) {
            this.behaviour = behaviour;
            tanks = behaviour.tanks;
            this.enforceVariety = enforceVariety;
            slots = SlotRangeCache.get(tanks.length);
            this.capacity = capacity;
        }

        @Override
        public FluidStack onExtract(FluidStack stack) {
            return removeMaxSize(stack, capacity);
        }

        @Override
        public int getMaxAmountPerStack() {
            return capacity;
        }

        @Override
        public int[] getAvailableSlots(@Nullable Direction side) {
            return slots;
        }

        @Override
        public boolean canInsert(int slot, FluidStack stack, @Nullable Direction dir) {
            if (!behaviour.insertionAllowed) {
                return false;
            }
            if (enforceVariety) {
                FluidStack fluid = tanks[slot].getFluid();
                if (fluid.isEmpty()) {
                    for (int i = 0; i < slot; i++) {
                        fluid = tanks[i].getFluid();
                        if (fluid.isEmpty()) {
                            continue;
                        }
                        if (matches(fluid, stack)) {
                            return false;
                        }
                    }
                    for (int i = slot + 1, size = tanks.length; i < size; i++) {
                        fluid = tanks[i].getFluid();
                        if (fluid.isEmpty()) {
                            continue;
                        }
                        if (matches(fluid, stack)) {
                            return false;
                        }
                    }
                } else {
                    return matches(fluid, stack);
                }
            }
            return true;
        }

        @Override
        public boolean canExtract(int slot, FluidStack stack, Direction dir) {
            return behaviour.extractionAllowed;
        }

        @Override
        public int size() {
            return tanks.length;
        }

        @Override
        public FluidStack getStack(int slot) {
            if (slot >= tanks.length) {
                return FluidStack.EMPTY;
            }
            return tanks[slot].getFluid();
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            if (slot >= tanks.length) {
                return;
            }
            TankSegment tank = tanks[slot];
            tank.setFluid(stack);
        }

        @Override
        public void markDirty() {
            for (TankSegment tank : tanks) {
                tank.markDirty();
            }
        }
    }

    public class TankSegment implements FluidInventory {
        protected LerpedFloat fluidLevel = LerpedFloat.linear().startWithValue(0).chase(0, 0.25, Chaser.EXP);
        protected FluidStack renderedFluid = FluidStack.EMPTY;
        protected FluidStack fluid = FluidStack.EMPTY;
        protected int capacity;

        public TankSegment(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public FluidStack onExtract(FluidStack stack) {
            return removeMaxSize(stack, capacity);
        }

        @Override
        public int getMaxAmountPerStack() {
            return capacity;
        }

        @Override
        public int size() {
            return 1;
        }

        public FluidStack getFluid() {
            return fluid;
        }

        public void setFluid(FluidStack fluid) {
            if (fluid != FluidStack.EMPTY) {
                setMaxSize(fluid, capacity);
            }
            this.fluid = fluid;
        }

        @Override
        public FluidStack getStack(int slot) {
            if (slot != 0) {
                return FluidStack.EMPTY;
            }
            return fluid;
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            if (slot == 0) {
                setFluid(stack);
            }
        }

        @Override
        public void markDirty() {
            if (!blockEntity.hasLevel()) {
                return;
            }
            fluidLevel.chase(fluid.getAmount() / (float) capacity, 0.25, Chaser.EXP);
            if (!getLevel().isClientSide()) {
                sendDataLazily();
            }
            if (blockEntity.isVirtual() && !fluid.isEmpty()) {
                renderedFluid = fluid;
            }
        }

        public FluidStack getRenderedFluid() {
            return renderedFluid;
        }

        public LerpedFloat getFluidLevel() {
            return fluidLevel;
        }

        public float getTotalUnits(float partialTicks) {
            return fluidLevel.getValue(partialTicks) * capacity;
        }

        public void write(ValueOutput view) {
            view.store("TankContent", FluidStack.OPTIONAL_CODEC, fluid);
            fluidLevel.write(view);
        }

        public void read(ValueInput view, boolean clientPacket) {
            fluid = view.read("TankContent", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY);
            fluidLevel.read(view, clientPacket);
            if (!fluid.isEmpty()) {
                renderedFluid = fluid;
            }
        }

        public boolean isEmpty(float partialTicks) {
            if (getRenderedFluid().isEmpty()) {
                return true;
            }
            return getTotalUnits(partialTicks) < 1;
        }

    }

    @Override
    public BehaviourType<?> getType() {
        return behaviourType;
    }
}
