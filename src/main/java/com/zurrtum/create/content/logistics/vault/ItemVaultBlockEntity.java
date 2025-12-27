package com.zurrtum.create.content.logistics.vault;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.api.packager.InventoryIdentifier;
import com.zurrtum.create.foundation.block.NeighborChangeListeningBlock;
import com.zurrtum.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.VersionedInventory;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.BitSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemVaultBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.Inventory, Clearable {

    protected Supplier<ItemInventory> itemCapability = null;
    protected InventoryIdentifier invId;

    protected ItemVaultHandler inventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected int radius;
    protected int length;
    protected Axis axis;

    public ItemVaultBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ITEM_VAULT, pos, state);

        inventory = new ItemVaultHandler();

        radius = 1;
        length = 1;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        Containers.dropContents(level, pos, inventory);
        level.removeBlockEntity(pos);
        ConnectivityHandler.splitMulti(this);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    protected void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide())
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    protected void updateComparators() {
        ItemVaultBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null)
            return;

        level.blockEntityChanged(controllerBE.worldPosition);

        BlockPos pos = controllerBE.getBlockPos();

        int radius = controllerBE.radius;
        int length = controllerBE.length;

        Axis axis = controllerBE.getMainConnectionAxis();

        int zMax = (axis == Axis.X ? radius : length);
        int xMax = (axis == Axis.Z ? radius : length);

        // Mutable position we'll use for the blocks we poke updates at.
        BlockPos.MutableBlockPos updatePos = new BlockPos.MutableBlockPos();
        // Mutable position we'll set to be the vault block next to the update position.
        BlockPos.MutableBlockPos provokingPos = new BlockPos.MutableBlockPos();

        for (int y = 0; y < radius; y++) {
            for (int z = 0; z < zMax; z++) {
                for (int x = 0; x < xMax; x++) {
                    // Emulate the effect of this line, but only for blocks along the surface of the vault:
                    // level.updateNeighbourForOutputSignal(pos.offset(x, y, z), getBlockState().getBlock());
                    // That method pokes all 6 directions in order. We want to preserve the update order
                    // but skip the wasted work of checking other blocks that are part of this vault.

                    var sectionX = SectionPos.blockToSectionCoord(pos.getX() + x);
                    var sectionZ = SectionPos.blockToSectionCoord(pos.getZ() + z);
                    if (!level.hasChunk(sectionX, sectionZ)) {
                        continue;
                    }
                    provokingPos.setWithOffset(pos, x, y, z);

                    // Technically all this work is wasted for the inner blocks of a long 3x3 vault, but
                    // this is fast enough and relatively simple.
                    Block provokingBlock = level.getBlockState(provokingPos).getBlock();

                    // The 6 calls below should match the order of Direction.values().
                    if (y == 0) {
                        updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.DOWN);
                    }
                    if (y == radius - 1) {
                        updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.UP);
                    }
                    if (z == 0) {
                        updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.NORTH);
                    }
                    if (z == zMax - 1) {
                        updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.SOUTH);
                    }
                    if (x == 0) {
                        updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.WEST);
                    }
                    if (x == xMax - 1) {
                        updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.EAST);
                    }
                }
            }
        }
    }

    /**
     * See {@link Level#updateNeighbourForOutputSignal(BlockPos, Block)}.
     */
    private static void updateComparatorsInner(
        Level level,
        Block provokingBlock,
        BlockPos provokingPos,
        BlockPos.MutableBlockPos updatePos,
        Direction direction
    ) {
        updatePos.setWithOffset(provokingPos, direction);

        var sectionX = SectionPos.blockToSectionCoord(updatePos.getX());
        var sectionZ = SectionPos.blockToSectionCoord(updatePos.getZ());
        if (!level.hasChunk(sectionX, sectionZ)) {
            return;
        }

        BlockState blockstate = level.getBlockState(updatePos);
        if (blockstate.getBlock() instanceof NeighborChangeListeningBlock block) {
            block.onNeighborChange(blockstate, level, updatePos, provokingPos);
        }

        if (blockstate.is(Blocks.COMPARATOR)) {
            level.neighborChanged(blockstate, updatePos, provokingBlock, null, false);
        } else if (blockstate.isRedstoneConductor(level, updatePos)) {
            updatePos.move(direction);
            blockstate = level.getBlockState(updatePos);
            if (blockstate.is(Blocks.COMPARATOR)) {
                level.neighborChanged(blockstate, updatePos, provokingBlock, null, false);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
            onPositionChanged();
            return;
        }

        if (updateConnectivity)
            updateConnectivity();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX() && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ItemVaultBlockEntity getControllerBE() {
        if (isController())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof ItemVaultBlockEntity)
            return (ItemVaultBlockEntity) blockEntity;
        return null;
    }

    public void removeController(boolean keepContents) {
        if (level.isClientSide())
            return;
        updateConnectivity = true;
        controller = null;
        radius = 1;
        length = 1;

        BlockState state = getBlockState();
        if (ItemVaultBlock.isVault(state)) {
            state = state.setValue(ItemVaultBlock.LARGE, false);
            getLevel().setBlock(worldPosition, state, Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE | Block.UPDATE_KNOWN_SHAPE);
        }

        itemCapability = null;
        setChanged();
        sendData();
    }

    @Override
    public void setController(BlockPos controller) {
        if (level.isClientSide() && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        itemCapability = null;
        setChanged();
        sendData();
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = radius;
        int prevLength = length;

        updateConnectivity = view.getBooleanOr("Uninitialized", false);

        lastKnownPos = view.read("LastKnownPos", BlockPos.CODEC).orElse(null);

        controller = view.read("Controller", BlockPos.CODEC).orElse(null);

        if (isController()) {
            radius = view.getIntOr("Size", 0);
            length = view.getIntOr("Length", 0);
        }

        if (!clientPacket) {
            inventory.read(view);
            return;
        }

        boolean changeOfController = controllerBefore == null ? controller != null : !controllerBefore.equals(controller);
        if (hasLevel() && (changeOfController || prevSize != radius || prevLength != length))
            level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        if (updateConnectivity)
            view.putBoolean("Uninitialized", true);

        if (lastKnownPos != null)
            view.store("LastKnownPos", BlockPos.CODEC, lastKnownPos);
        if (isController()) {
            view.putInt("Size", radius);
            view.putInt("Length", length);
        } else {
            view.store("Controller", BlockPos.CODEC, controller);
        }

        super.write(view, clientPacket);

        if (!clientPacket) {
            view.putString("StorageType", "CombinedInv");
            inventory.write(view);
        }
    }

    @Override
    public void clearContent() {
        inventory.clearContent();
    }

    public ItemVaultHandler getInventoryOfBlock() {
        return inventory;
    }

    public InventoryIdentifier getInvId() {
        // ensure capability is up to date first, which sets the ID
        this.initCapability();
        return this.invId;
    }

    public void applyInventoryToBlock(ItemStackHandler handler) {
        int size = handler.getContainerSize();
        for (int i = 0; i < size; i++) {
            inventory.setItem(i, handler.getItem(i));
        }
        for (int i = size, max = inventory.getContainerSize(); i < max; i++) {
            inventory.setItem(i, ItemStack.EMPTY);
        }
    }

    public void initCapability() {
        if (!isController()) {
            ItemVaultBlockEntity controllerBE = getControllerBE();
            if (controllerBE == null)
                return;
            if (controllerBE.itemCapability == null || controllerBE.itemCapability.get() == null)
                controllerBE.initCapability();
            itemCapability = () -> {
                if (controllerBE.isRemoved())
                    return null;
                if (controllerBE.itemCapability == null)
                    return null;
                return controllerBE.itemCapability.get();
            };
            invId = controllerBE.invId;
            return;
        }

        boolean alongZ = ItemVaultBlock.getVaultBlockAxis(getBlockState()) == Axis.Z;
        ItemVaultHandler[] invs = new ItemVaultHandler[length * radius * radius];
        Find:
        for (int yOffset = 0; yOffset < length; yOffset++) {
            for (int xOffset = 0; xOffset < radius; xOffset++) {
                for (int zOffset = 0; zOffset < radius; zOffset++) {
                    BlockPos vaultPos = alongZ ? worldPosition.offset(xOffset, zOffset, yOffset) : worldPosition.offset(yOffset, xOffset, zOffset);
                    ItemVaultBlockEntity vaultAt = ConnectivityHandler.partAt(AllBlockEntityTypes.ITEM_VAULT, level, vaultPos);
                    if (vaultAt == null) {
                        invs = null;
                        break Find;
                    }
                    invs[yOffset * radius * radius + xOffset * radius + zOffset] = vaultAt.inventory;
                }
            }
        }

        if (invs == null) {
            itemCapability = null;
        } else {
            ConnectedItemVaultHandler capability = new ConnectedItemVaultHandler(invs);
            itemCapability = () -> capability;
        }

        // build an identifier encompassing all component vaults
        BlockPos farCorner = alongZ ? worldPosition.offset(radius, radius, length) : worldPosition.offset(length, radius, radius);
        BoundingBox bounds = BoundingBox.fromCorners(this.worldPosition, farCorner);
        this.invId = new InventoryIdentifier.Bounds(bounds);
    }

    public static int getMaxLength(int radius) {
        return radius * 3;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = this.getBlockState();
        if (ItemVaultBlock.isVault(state)) { // safety
            level.setBlock(getBlockPos(), state.setValue(ItemVaultBlock.LARGE, radius > 2), Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE);
        }
        itemCapability = null;
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return getMainAxisOf(this);
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y)
            return getMaxWidth();
        return getMaxLength(width);
    }

    @Override
    public int getMaxWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return length;
    }

    @Override
    public int getWidth() {
        return radius;
    }

    @Override
    public void setHeight(int height) {
        this.length = height;
    }

    @Override
    public void setWidth(int width) {
        this.radius = width;
    }

    @Override
    public boolean hasInventory() {
        return true;
    }

    public class ItemVaultHandler implements ItemInventory {
        private final int size;
        protected final NonNullList<ItemStack> stacks;

        public ItemVaultHandler() {
            size = AllConfigs.server().logistics.vaultCapacity.get();
            stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        }

        @Override
        public int getContainerSize() {
            return size;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (slot >= size) {
                return ItemStack.EMPTY;
            }
            return stacks.get(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= size) {
                return;
            }
            stacks.set(slot, stack);
        }

        @Override
        public void setChanged() {
            level.blockEntityChanged(worldPosition);
        }

        public void write(ValueOutput view) {
            ValueOutput.TypedOutputList<ItemStack> list = view.list("Inventory", ItemStack.CODEC);
            for (ItemStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                list.add(stack);
            }
        }

        public void read(ValueInput view) {
            ValueInput.TypedInputList<ItemStack> list = view.listOrEmpty("Inventory", ItemStack.CODEC);
            int i = 0;
            for (ItemStack itemStack : list) {
                stacks.set(i++, itemStack);
            }
            for (int size = stacks.size(); i < size; i++) {
                stacks.set(i, ItemStack.EMPTY);
            }
        }
    }

    public class ConnectedItemVaultHandler implements ItemInventory, VersionedInventory {
        private final ItemVaultHandler[] itemHandler;
        private final int vaultCapacity;
        private final int size;
        private final int id;
        private final BitSet accessed;
        private int version;

        public ConnectedItemVaultHandler(ItemVaultHandler[] invs) {
            vaultCapacity = AllConfigs.server().logistics.vaultCapacity.get();
            itemHandler = invs;
            size = vaultCapacity * invs.length;
            id = idGenerator.getAndIncrement();
            accessed = new BitSet(invs.length);
            version = 0;
        }

        @Override
        public int getContainerSize() {
            return size;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (slot >= size) {
                return ItemStack.EMPTY;
            }
            int i = slot / vaultCapacity;
            accessed.set(i);
            return itemHandler[i].getItem(slot % vaultCapacity);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= size) {
                return;
            }
            int i = slot / vaultCapacity;
            ItemVaultHandler handler = itemHandler[i];
            handler.setItem(slot % vaultCapacity, stack);
            handler.setChanged();
        }

        @Override
        public int insert(ItemStack stack) {
            if (stack.isEmpty()) {
                return 0;
            }
            int maxAmount = stack.getCount();
            int remaining = maxAmount;
            for (ItemVaultHandler handler : itemHandler) {
                int insert = handler.insert(stack);
                if (remaining == insert) {
                    incrementVersion();
                    stack.setCount(maxAmount);
                    return maxAmount;
                }
                if (insert == 0) {
                    continue;
                }
                stack.setCount(remaining -= insert);
            }
            if (remaining == maxAmount) {
                return 0;
            }
            incrementVersion();
            stack.setCount(maxAmount);
            return maxAmount - remaining;
        }

        @Override
        public int extract(ItemStack stack) {
            if (stack.isEmpty()) {
                return 0;
            }
            int maxAmount = stack.getCount();
            int remaining = maxAmount;
            for (ItemVaultHandler handler : itemHandler) {
                int extract = handler.extract(stack);
                if (remaining == extract) {
                    incrementVersion();
                    stack.setCount(maxAmount);
                    return maxAmount;
                }
                if (extract == 0) {
                    continue;
                }
                stack.setCount(remaining -= extract);
            }
            if (remaining == maxAmount) {
                return 0;
            }
            incrementVersion();
            stack.setCount(maxAmount);
            return maxAmount - remaining;
        }

        @Override
        public ItemStack extract(Predicate<ItemStack> predicate, int maxAmount) {
            if (maxAmount == 0) {
                return ItemStack.EMPTY;
            }
            for (int i = 0, size = itemHandler.length; i < size; i++) {
                ItemStack findStack = itemHandler[i].extract(predicate, maxAmount);
                if (findStack == ItemStack.EMPTY) {
                    continue;
                }
                int extract = findStack.getCount();
                if (extract == maxAmount) {
                    incrementVersion();
                    return findStack;
                }
                i++;
                if (i == size) {
                    incrementVersion();
                    return findStack;
                }
                int remaining = maxAmount - extract;
                for (; i < size; i++) {
                    extract = itemHandler[i].extract(findStack);
                    if (remaining == extract) {
                        incrementVersion();
                        findStack.setCount(maxAmount);
                        return findStack;
                    }
                    if (extract == 0) {
                        continue;
                    }
                    findStack.setCount(remaining -= extract);
                }
                incrementVersion();
                findStack.setCount(maxAmount - remaining);
                return findStack;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount) {
            if (maxAmount == 0) {
                return ItemStack.EMPTY;
            }
            for (int i = 0, size = itemHandler.length; i < size; i++) {
                ItemStack findStack = itemHandler[i].count(predicate, maxAmount);
                if (findStack.isEmpty()) {
                    continue;
                }
                int count = findStack.getCount();
                if (count == maxAmount) {
                    itemHandler[i].extract(findStack);
                    incrementVersion();
                    return findStack;
                }
                i++;
                if (i == size) {
                    break;
                }
                int[] extracts = new int[size];
                int remaining = maxAmount - count;
                for (; i < size; i++) {
                    int extract = itemHandler[i].count(findStack, remaining);
                    if (extract == 0) {
                        continue;
                    }
                    extracts[i] = extract;
                    if (remaining > extract) {
                        remaining -= extract;
                        continue;
                    }
                    itemHandler[0].extract(findStack);
                    for (int j = 1; j <= i; j++) {
                        extract = extracts[j];
                        if (extract == 0) {
                            continue;
                        }
                        findStack.setCount(extract);
                        itemHandler[j].extract(findStack);
                    }
                    incrementVersion();
                    findStack.setCount(maxAmount);
                    return findStack;
                }
            }
            return ItemStack.EMPTY;
        }

        public int getVersion() {
            return version;
        }

        public int getId() {
            return id;
        }

        @Override
        public void setChanged() {
            for (ItemVaultHandler inventory : itemHandler) {
                inventory.setChanged();
            }
            incrementVersion();
        }

        private void incrementVersion() {
            updateComparators();
            version++;
        }
    }
}
