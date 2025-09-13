package com.zurrtum.create.content.fluids.tank;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock.Shape;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static java.lang.Math.abs;

public class FluidTankBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.Fluid {

    private static final int MAX_SIZE = 3;

    public FluidInventory fluidCapability;
    protected boolean forceFluidLevelUpdate;
    protected FluidTank tankInventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected boolean updateCapability;
    public boolean window;
    protected int luminosity;
    protected int width;
    protected int height;

    public BoilerData boiler;

    private static final int SYNC_RATE = 8;
    protected int syncCooldown;
    protected boolean queuedSync;

    // For rendering purposes only
    private LerpedFloat fluidLevel;

    public FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tankInventory = createInventory();
        forceFluidLevelUpdate = true;
        updateConnectivity = false;
        updateCapability = false;
        window = true;
        height = 1;
        width = 1;
        boiler = new BoilerData();
        refreshCapability();
    }

    public static FluidTankBlockEntity tank(BlockPos pos, BlockState state) {
        return new FluidTankBlockEntity(AllBlockEntityTypes.FLUID_TANK, pos, state);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        world.removeBlockEntity(pos);
        ConnectivityHandler.splitMulti(this);
    }

    protected FluidTank createInventory() {
        return new FluidTankInventory(getCapacityMultiplier());
    }

    protected void updateConnectivity() {
        updateConnectivity = false;
        if (world.isClient)
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync)
                sendData();
        }

        if (lastKnownPos == null)
            lastKnownPos = pos;
        else if (!lastKnownPos.equals(pos) && pos != null) {
            onPositionChanged();
            return;
        }

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }
        if (updateConnectivity)
            updateConnectivity();
        if (fluidLevel != null)
            fluidLevel.tickChaser();
        if (isController())
            boiler.tick(this);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (isController())
            boiler.updateOcclusion(this);
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || pos.getX() == controller.getX() && pos.getY() == controller.getY() && pos.getZ() == controller.getZ();
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
        if (world.isClient)
            invalidateRenderBoundingBox();
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = pos;
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        if (!hasWorld())
            return;

        int luminosity = (int) (newFluidStack.getFluid().getDefaultState().getBlockState().getLuminance() / 1.2f);
        boolean reversed = false;
        int maxY = (int) ((getFillState() * height) + 1);

        for (int yOffset = 0; yOffset < height; yOffset++) {
            boolean isBright = reversed ? (height - yOffset <= maxY) : (yOffset < maxY);
            int actualLuminosity = isBright ? luminosity : luminosity > 0 ? 1 : 0;

            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {
                    BlockPos pos = this.pos.add(xOffset, yOffset, zOffset);
                    FluidTankBlockEntity tankAt = ConnectivityHandler.partAt(getType(), world, pos);
                    if (tankAt == null)
                        continue;
                    world.updateComparators(pos, tankAt.getCachedState().getBlock());
                    if (tankAt.luminosity == actualLuminosity)
                        continue;
                    tankAt.setLuminosity(actualLuminosity);
                }
            }
        }

        if (!world.isClient) {
            markDirty();
            sendData();
        }

        if (isVirtual()) {
            if (fluidLevel == null)
                fluidLevel = LerpedFloat.linear().startWithValue(getFillState());
            fluidLevel.chase(getFillState(), .5f, Chaser.EXP);
        }
    }

    protected void setLuminosity(int luminosity) {
        if (world.isClient)
            return;
        if (this.luminosity == luminosity)
            return;
        this.luminosity = luminosity;
        updateStateLuminosity();
        sendData();
    }

    protected void updateStateLuminosity() {
        if (world.isClient)
            return;
        int actualLuminosity = luminosity;
        FluidTankBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null || !controllerBE.window)
            actualLuminosity = 0;
        refreshBlockState();
        BlockState state = getCachedState();
        if (state.get(FluidTankBlock.LIGHT_LEVEL) != actualLuminosity) {
            world.setBlockState(pos, state.with(FluidTankBlock.LIGHT_LEVEL, actualLuminosity), 23);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public FluidTankBlockEntity getControllerBE() {
        if (isController() || !hasWorld())
            return this;
        BlockEntity blockEntity = world.getBlockEntity(controller);
        if (blockEntity instanceof FluidTankBlockEntity)
            return (FluidTankBlockEntity) blockEntity;
        return null;
    }

    public void applyFluidTankSize(int blocks) {
        int capacity = blocks * getCapacityMultiplier();
        tankInventory.setCapacity(capacity);
        FluidStack stack = tankInventory.getFluid();
        if (stack.getAmount() > capacity) {
            stack.setAmount(capacity);
            tankInventory.markDirty();
        }
        forceFluidLevelUpdate = true;
    }

    public void removeController(boolean keepFluids) {
        if (world.isClient)
            return;
        updateConnectivity = true;
        if (!keepFluids)
            applyFluidTankSize(1);
        controller = null;
        width = 1;
        height = 1;
        boiler.clear();
        onFluidStackChanged(tankInventory.getFluid());

        BlockState state = getCachedState();
        if (FluidTankBlock.isTank(state)) {
            state = state.with(FluidTankBlock.BOTTOM, true);
            state = state.with(FluidTankBlock.TOP, true);
            state = state.with(FluidTankBlock.SHAPE, window ? Shape.WINDOW : Shape.PLAIN);
            getWorld().setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.NO_REDRAW | Block.FORCE_STATE);
        }

        refreshCapability();
        markDirty();
        sendData();
    }

    public void toggleWindows() {
        FluidTankBlockEntity be = getControllerBE();
        if (be == null)
            return;
        if (be.boiler.isActive())
            return;
        be.setWindows(!be.window);
    }

    public void updateBoilerTemperature() {
        FluidTankBlockEntity be = getControllerBE();
        if (be == null)
            return;
        if (!be.boiler.isActive())
            return;
        be.boiler.needsHeatLevelUpdate = true;
    }

    public void sendDataImmediately() {
        syncCooldown = 0;
        queuedSync = false;
        sendData();
    }

    @Override
    public void sendData() {
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        super.sendData();
        queuedSync = false;
        syncCooldown = SYNC_RATE;
    }

    public void setWindows(boolean window) {
        this.window = window;
        for (int yOffset = 0; yOffset < height; yOffset++) {
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {

                    BlockPos pos = this.pos.add(xOffset, yOffset, zOffset);
                    BlockState blockState = world.getBlockState(pos);
                    if (!FluidTankBlock.isTank(blockState))
                        continue;

                    Shape shape = Shape.PLAIN;
                    if (window) {
                        // SIZE 1: Every tank has a window
                        if (width == 1)
                            shape = Shape.WINDOW;
                        // SIZE 2: Every tank has a corner window
                        if (width == 2)
                            shape = xOffset == 0 ? zOffset == 0 ? Shape.WINDOW_NW : Shape.WINDOW_SW : zOffset == 0 ? Shape.WINDOW_NE : Shape.WINDOW_SE;
                        // SIZE 3: Tanks in the center have a window
                        if (width == 3 && abs(abs(xOffset) - abs(zOffset)) == 1)
                            shape = Shape.WINDOW;
                    }

                    world.setBlockState(
                        pos,
                        blockState.with(FluidTankBlock.SHAPE, shape),
                        Block.NOTIFY_LISTENERS | Block.NO_REDRAW | Block.FORCE_STATE
                    );
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof FluidTankBlockEntity tankAt)
                        tankAt.updateStateLuminosity();
                    world.getChunkManager().getLightingProvider().checkBlock(pos);
                }
            }
        }
    }

    public void updateBoilerState() {
        if (!isController())
            return;

        boolean wasBoiler = boiler.isActive();
        boolean changed = boiler.evaluate(this);

        if (wasBoiler != boiler.isActive()) {
            if (boiler.isActive())
                setWindows(false);

            for (int yOffset = 0; yOffset < height; yOffset++)
                for (int xOffset = 0; xOffset < width; xOffset++)
                    for (int zOffset = 0; zOffset < width; zOffset++)
                        if (world.getBlockEntity(pos.add(xOffset, yOffset, zOffset)) instanceof FluidTankBlockEntity fbe)
                            fbe.refreshCapability();
        }

        if (changed) {
            notifyUpdate();
            boiler.checkPipeOrganAdvancement(this);
        }
    }

    @Override
    public void setController(BlockPos controller) {
        if (world.isClient && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        refreshCapability();
        markDirty();
        sendData();
    }

    public void refreshCapability() {
        fluidCapability = handlerForCapability();
    }

    private FluidInventory handlerForCapability() {
        return isController() ? (boiler.isActive() ? boiler.createHandler() : tankInventory) : ((getControllerBE() != null) ? getControllerBE().handlerForCapability() : new FluidTank(
            0));
    }

    @Override
    public BlockPos getController() {
        return isController() ? pos : controller;
    }

    @Override
    protected Box createRenderBoundingBox() {
        if (isController())
            return super.createRenderBoundingBox().stretch(width - 1, height - 1, width - 1);
        else
            return super.createRenderBoundingBox();
    }

    @Nullable
    public FluidTankBlockEntity getOtherFluidTankBlockEntity(Direction direction) {
        BlockEntity otherBE = world.getBlockEntity(pos.offset(direction));
        if (otherBE instanceof FluidTankBlockEntity)
            return (FluidTankBlockEntity) otherBE;
        return null;
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = width;
        int prevHeight = height;
        int prevLum = luminosity;

        updateConnectivity = view.getBoolean("Uninitialized", false);
        luminosity = view.getInt("Luminosity", 0);

        lastKnownPos = view.read("LastKnownPos", BlockPos.CODEC).orElse(null);

        controller = view.read("Controller", BlockPos.CODEC).orElse(null);

        if (isController()) {
            window = view.getBoolean("Window", false);
            width = view.getInt("Size", 0);
            height = view.getInt("Height", 0);
            tankInventory.setCapacity(getTotalTankSize() * getCapacityMultiplier());

            tankInventory.read(view);
        }

        boiler.read(view.getReadView("Boiler"), width * width * height);

        if (view.getBoolean("ForceFluidLevel", false) || fluidLevel == null)
            fluidLevel = LerpedFloat.linear().startWithValue(getFillState());

        updateCapability = true;

        if (!clientPacket)
            return;

        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevSize != width || prevHeight != height) {
            if (hasWorld())
                world.updateListeners(getPos(), getCachedState(), getCachedState(), 16);
            if (isController())
                tankInventory.setCapacity(getCapacityMultiplier() * getTotalTankSize());
            invalidateRenderBoundingBox();
        }
        if (isController()) {
            float fillState = getFillState();
            if (view.getBoolean("ForceFluidLevel", false) || fluidLevel == null)
                fluidLevel = LerpedFloat.linear().startWithValue(fillState);
            fluidLevel.chase(fillState, 0.5f, Chaser.EXP);
        }
        if (luminosity != prevLum && hasWorld())
            world.getChunkManager().getLightingProvider().checkBlock(pos);

        if (view.getBoolean("LazySync", false))
            fluidLevel.chase(fluidLevel.getChaseTarget(), 0.125f, Chaser.EXP);
    }

    public float getFillState() {
        return (float) tankInventory.getFluid().getAmount() / tankInventory.getMaxAmountPerStack();
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        if (updateConnectivity)
            view.putBoolean("Uninitialized", true);
        boiler.write(view.get("Boiler"));
        if (lastKnownPos != null)
            view.put("LastKnownPos", BlockPos.CODEC, lastKnownPos);
        if (!isController())
            view.put("Controller", BlockPos.CODEC, controller);
        if (isController()) {
            view.putBoolean("Window", window);
            tankInventory.write(view);
            view.putInt("Size", width);
            view.putInt("Height", height);
        }
        view.putInt("Luminosity", luminosity);
        super.write(view, clientPacket);

        if (!clientPacket)
            return;
        if (forceFluidLevelUpdate)
            view.putBoolean("ForceFluidLevel", true);
        if (queuedSync)
            view.putBoolean("LazySync", true);
        forceFluidLevelUpdate = false;
    }

    @Override
    public void writeSafe(WriteView view) {
        if (isController()) {
            view.putBoolean("Window", window);
            view.putInt("Size", width);
            view.putInt("Height", height);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.STEAM_ENGINE_MAXED, AllAdvancements.PIPE_ORGAN);
    }

    public FluidTank getTankInventory() {
        return tankInventory;
    }

    public int getTotalTankSize() {
        return width * width * height;
    }

    public static int getMaxSize() {
        return MAX_SIZE;
    }

    public static int getCapacityMultiplier() {
        return AllConfigs.server().fluids.fluidTankCapacity.get() * BucketFluidInventory.CAPACITY;
    }

    public static int getMaxHeight() {
        return AllConfigs.server().fluids.fluidTankMaxHeight.get();
    }

    public LerpedFloat getFluidLevel() {
        return fluidLevel;
    }

    public void setFluidLevel(LerpedFloat fluidLevel) {
        this.fluidLevel = fluidLevel;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = getCachedState();
        if (FluidTankBlock.isTank(state)) { // safety
            state = state.with(FluidTankBlock.BOTTOM, getController().getY() == pos.getY());
            state = state.with(FluidTankBlock.TOP, getController().getY() + height - 1 == pos.getY());
            world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.NO_REDRAW);
        }
        if (isController())
            setWindows(window);
        onFluidStackChanged(tankInventory.getFluid());
        updateBoilerState();
        markDirty();
    }

    @Override
    public void setExtraData(@Nullable Object data) {
        if (data instanceof Boolean)
            window = (boolean) data;
    }

    @Override
    @Nullable
    public Object getExtraData() {
        return window;
    }

    @Override
    public Object modifyExtraData(Object data) {
        if (data instanceof Boolean windows) {
            windows |= window;
            return windows;
        }
        return data;
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y)
            return getMaxHeight();
        return getMaxWidth();
    }

    @Override
    public int getMaxWidth() {
        return MAX_SIZE;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public boolean hasTank() {
        return true;
    }

    @Override
    public int getTankSize(int tank) {
        return getCapacityMultiplier();
    }

    @Override
    public void setTankSize(int tank, int blocks) {
        applyFluidTankSize(blocks);
    }

    @Override
    public FluidTank getTank(int tank) {
        return tankInventory;
    }

    @Override
    public FluidStack getFluid(int tank) {
        return tankInventory.getFluid().copy();
    }

    @Override
    public boolean matches(FluidStack stack, FluidStack otherStack) {
        return tankInventory.matches(stack, otherStack);
    }

    public class FluidTankInventory extends FluidTank {
        public FluidTankInventory(int capacity) {
            super(capacity);
        }

        @Override
        public void markDirty() {
            onFluidStackChanged(fluid);
        }
    }
}
