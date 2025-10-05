package com.zurrtum.create.content.redstone.thresholdSwitch;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.processing.recipe.ProcessingInventory;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase.InterfaceProvider;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.tick.TickPriority;

import java.util.List;

public class ThresholdSwitchBlockEntity extends SmartBlockEntity {

    public int onWhenAbove;
    public int offWhenBelow;

    public int currentMinLevel;
    public int currentLevel;
    public int currentMaxLevel;
    public boolean inStacks;

    private boolean redstoneState;
    private boolean inverted;
    private boolean poweredAfterDelay;

    private ServerFilteringBehaviour filtering;
    private InvManipulationBehaviour observedInventory;
    private TankManipulationBehaviour observedTank;
    private VersionedInventoryTrackerBehaviour invVersionTracker;

    //TODO
    //    private static final List<ThresholdSwitchCompat> COMPAT = List.of(new FunctionalStorage(), new SophisticatedStorage(), new StorageDrawers());

    public ThresholdSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.THRESHOLD_SWITCH, pos, state);
        onWhenAbove = 128;
        offWhenBelow = 64;
        currentLevel = -1;
        redstoneState = false;
        inverted = false;
        poweredAfterDelay = false;
        setLazyTickRate(10);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        onWhenAbove = view.getInt("OnAboveAmount", 0);
        offWhenBelow = view.getInt("OffBelowAmount", 0);
        currentLevel = view.getInt("CurrentAmount", 0);
        currentMinLevel = view.getInt("CurrentMinAmount", 0);
        currentMaxLevel = view.getInt("CurrentMaxAmount", 0);
        inStacks = view.getBoolean("InStacks", false);
        redstoneState = view.getBoolean("Powered", false);
        inverted = view.getBoolean("Inverted", false);
        poweredAfterDelay = view.getBoolean("PoweredAfterDelay", false);
        super.read(view, clientPacket);
    }

    protected void writeCommon(WriteView view) {
        view.putFloat("OnAboveAmount", onWhenAbove);
        view.putFloat("OffBelowAmount", offWhenBelow);
        view.putBoolean("Inverted", inverted);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        writeCommon(view);
        view.putInt("CurrentAmount", currentLevel);
        view.putInt("CurrentMinAmount", currentMinLevel);
        view.putInt("CurrentMaxAmount", currentMaxLevel);
        view.putBoolean("InStacks", inStacks);
        view.putBoolean("Powered", redstoneState);
        view.putBoolean("PoweredAfterDelay", poweredAfterDelay);
        super.write(view, clientPacket);
    }

    @Override
    public void writeSafe(WriteView view) {
        writeCommon(view);
        super.writeSafe(view);
    }

    public int getMinLevel() {
        return currentMinLevel;
    }

    public int getStockLevel() {
        return currentLevel;
    }

    public int getMaxLevel() {
        return currentMaxLevel;
    }

    public void updateCurrentLevel() {
        boolean changed = false;
        int prevLevel = currentLevel;
        int prevMaxLevel = currentMaxLevel;

        BlockPos target = getTargetPos();
        BlockEntity targetBlockEntity = world.getBlockEntity(target);

        observedInventory.findNewCapability();
        observedTank.findNewCapability();

        if (targetBlockEntity instanceof ThresholdSwitchObservable observable) {
            currentMinLevel = observable.getMinValue();
            currentLevel = observable.getCurrentValue();
            currentMaxLevel = observable.getMaxValue();

		/*} else if (StorageDrawers.isDrawer(targetBlockEntity) && observedInventory.hasInventory()) {
			currentMinLevel = 0;
			currentLevel = StorageDrawers.getItemCount(observedInventory.getInventory(), filtering);
			currentMaxLevel = StorageDrawers.getTotalStorageSpace(observedInventory.getInventory());
		*/

        } else if (observedInventory.hasInventory() || observedTank.hasInventory()) {
            currentMinLevel = 0;
            currentLevel = 0;
            currentMaxLevel = 0;

            if (observedInventory.hasInventory()) {

                // Item inventory
                Inventory inv = observedInventory.getInventory();
                if (invVersionTracker.stillWaiting(inv)) {
                    currentLevel = prevLevel;
                    currentMaxLevel = prevMaxLevel;

                } else {
                    invVersionTracker.awaitNewVersion(inv);
                    for (int slot = 0, size = inv.size(); slot < size; slot++) {
                        ItemStack stackInSlot = inv.getStack(slot);
                        int space = stackInSlot.isEmpty() ? 64 : inv.getMaxCount(stackInSlot);
                        if (space == 0)
                            continue;

                        currentMaxLevel += space;
                        if (filtering.test(stackInSlot))
                            currentLevel += stackInSlot.getCount();
                    }
                }
            }

            if (observedTank.hasInventory()) {
                // Fluid inventory
                FluidInventory tank = observedTank.getInventory();
                for (int slot = 0, size = tank.size(); slot < size; slot++) {
                    FluidStack stackInSlot = tank.getStack(slot);
                    int space = tank.getMaxAmount(stackInSlot);
                    if (space == 0)
                        continue;

                    currentMaxLevel += space;
                    if (filtering.test(stackInSlot))
                        currentLevel += stackInSlot.getAmount();
                }
            }

        } else {
            // No compatible inventories found
            currentMinLevel = -1;
            currentMaxLevel = -1;
            if (currentLevel == -1)
                return;

            world.setBlockState(pos, getCachedState().with(ThresholdSwitchBlock.LEVEL, 0), Block.NOTIFY_ALL);
            currentLevel = -1;
            redstoneState = false;
            sendData();
            scheduleBlockTick();
            return;
        }

        currentLevel = MathHelper.clamp(currentLevel, currentMinLevel, currentMaxLevel);
        changed = currentLevel != prevLevel;

        boolean previouslyPowered = redstoneState;
        if (redstoneState && currentLevel <= offWhenBelow)
            redstoneState = false;
        else if (!redstoneState && currentLevel >= onWhenAbove)
            redstoneState = true;
        boolean update = previouslyPowered != redstoneState;

        int displayLevel = 0;
        float normedLevel = (float) (currentLevel - currentMinLevel) / (currentMaxLevel - currentMinLevel);
        if (currentLevel > 0)
            displayLevel = (int) (1 + normedLevel * 4);
        world.setBlockState(pos, getCachedState().with(ThresholdSwitchBlock.LEVEL, displayLevel), update ? 3 : 2);

        if (update)
            scheduleBlockTick();

        if (changed || update) {
            DisplayLinkBlock.notifyGatherers(world, pos);
            notifyUpdate();
        }
    }

    private boolean isSuitableInventory(BlockEntity be) {
        return be != null && !(be instanceof StockTickerBlockEntity || ItemHelper.getInventory(
            world,
            be.getPos(),
            null,
            be,
            null
        ) instanceof ProcessingInventory);
    }

    public BlockPos getTargetPos() {
        return pos.offset(ThresholdSwitchBlock.getTargetDirection(getCachedState()));
    }

    public ItemStack getDisplayItemForScreen() {
        BlockPos target = getTargetPos();
        return new ItemStack(world.getBlockState(target).getBlock());
    }

    public static enum ThresholdType {
        UNSUPPORTED,
        ITEM,
        FLUID,
        CUSTOM;
    }

    public ThresholdType getTypeOfCurrentTarget() {
        if (observedInventory.hasInventory())
            return ThresholdType.ITEM;
        if (observedTank.hasInventory())
            return ThresholdType.FLUID;
        if (world.getBlockEntity(getTargetPos()) instanceof ThresholdSwitchObservable)
            return ThresholdType.CUSTOM;
        return ThresholdType.UNSUPPORTED;
    }

    protected void scheduleBlockTick() {
        Block block = getCachedState().getBlock();
        if (!world.getBlockTickScheduler().isTicking(pos, block))
            world.scheduleBlockTick(pos, block, 2, TickPriority.NORMAL);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (world.isClient())
            return;
        updateCurrentLevel();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(filtering = new ServerFilteringBehaviour(this).withCallback($ -> {
            this.updateCurrentLevel();
            invVersionTracker.reset();
        }));

        behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));

        InterfaceProvider towardBlockFacing = (w, p, s) -> new BlockFace(p, DirectedDirectionalBlock.getTargetDirection(s));

        behaviours.add(observedInventory = new InvManipulationBehaviour(this, towardBlockFacing).bypassSidedness()
            .withFilter(this::isSuitableInventory));
        behaviours.add(observedTank = new TankManipulationBehaviour(this, towardBlockFacing).bypassSidedness());
    }

    public float getLevelForDisplay() {
        return currentLevel == -1 ? 0 : currentLevel;
    }

    public boolean getState() {
        return redstoneState;
    }

    public boolean shouldBePowered() {
        return inverted != redstoneState;
    }

    public void updatePowerAfterDelay() {
        poweredAfterDelay = shouldBePowered();
        world.updateNeighbors(pos, getCachedState().getBlock());
        sendData();
    }

    public boolean isPowered() {
        return poweredAfterDelay;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        if (inverted == this.inverted)
            return;
        this.inverted = inverted;
        updatePowerAfterDelay();
    }
}
