package com.zurrtum.create.content.logistics.vault;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.api.packager.InventoryIdentifier;
import com.zurrtum.create.foundation.block.NeighborChangeListeningBlock;
import com.zurrtum.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.VersionedInventory;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Clearable;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

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
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        ItemScatterer.spawn(world, pos, inventory);
        world.removeBlockEntity(pos);
        ConnectivityHandler.splitMulti(this);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    protected void updateConnectivity() {
        updateConnectivity = false;
        if (world.isClient())
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    protected void updateComparators() {
        ItemVaultBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null)
            return;

        world.markDirty(controllerBE.pos);

        BlockPos pos = controllerBE.getPos();

        int radius = controllerBE.radius;
        int length = controllerBE.length;

        Axis axis = controllerBE.getMainConnectionAxis();

        int zMax = (axis == Axis.X ? radius : length);
        int xMax = (axis == Axis.Z ? radius : length);

        // Mutable position we'll use for the blocks we poke updates at.
        BlockPos.Mutable updatePos = new BlockPos.Mutable();
        // Mutable position we'll set to be the vault block next to the update position.
        BlockPos.Mutable provokingPos = new BlockPos.Mutable();

        for (int y = 0; y < radius; y++) {
            for (int z = 0; z < zMax; z++) {
                for (int x = 0; x < xMax; x++) {
                    // Emulate the effect of this line, but only for blocks along the surface of the vault:
                    // level.updateNeighbourForOutputSignal(pos.offset(x, y, z), getBlockState().getBlock());
                    // That method pokes all 6 directions in order. We want to preserve the update order
                    // but skip the wasted work of checking other blocks that are part of this vault.

                    var sectionX = ChunkSectionPos.getSectionCoord(pos.getX() + x);
                    var sectionZ = ChunkSectionPos.getSectionCoord(pos.getZ() + z);
                    if (!world.isChunkLoaded(sectionX, sectionZ)) {
                        continue;
                    }
                    provokingPos.set(pos, x, y, z);

                    // Technically all this work is wasted for the inner blocks of a long 3x3 vault, but
                    // this is fast enough and relatively simple.
                    Block provokingBlock = world.getBlockState(provokingPos).getBlock();

                    // The 6 calls below should match the order of Direction.values().
                    if (y == 0) {
                        updateComparatorsInner(world, provokingBlock, provokingPos, updatePos, Direction.DOWN);
                    }
                    if (y == radius - 1) {
                        updateComparatorsInner(world, provokingBlock, provokingPos, updatePos, Direction.UP);
                    }
                    if (z == 0) {
                        updateComparatorsInner(world, provokingBlock, provokingPos, updatePos, Direction.NORTH);
                    }
                    if (z == zMax - 1) {
                        updateComparatorsInner(world, provokingBlock, provokingPos, updatePos, Direction.SOUTH);
                    }
                    if (x == 0) {
                        updateComparatorsInner(world, provokingBlock, provokingPos, updatePos, Direction.WEST);
                    }
                    if (x == xMax - 1) {
                        updateComparatorsInner(world, provokingBlock, provokingPos, updatePos, Direction.EAST);
                    }
                }
            }
        }
    }

    /**
     * See {@link World#updateComparators(BlockPos, Block)}.
     */
    private static void updateComparatorsInner(
        World level,
        Block provokingBlock,
        BlockPos provokingPos,
        BlockPos.Mutable updatePos,
        Direction direction
    ) {
        updatePos.set(provokingPos, direction);

        var sectionX = ChunkSectionPos.getSectionCoord(updatePos.getX());
        var sectionZ = ChunkSectionPos.getSectionCoord(updatePos.getZ());
        if (!level.isChunkLoaded(sectionX, sectionZ)) {
            return;
        }

        BlockState blockstate = level.getBlockState(updatePos);
        if (blockstate.getBlock() instanceof NeighborChangeListeningBlock block) {
            block.onNeighborChange(blockstate, level, updatePos, provokingPos);
        }

        if (blockstate.isOf(Blocks.COMPARATOR)) {
            level.updateNeighbor(blockstate, updatePos, provokingBlock, null, false);
        } else if (blockstate.isSolidBlock(level, updatePos)) {
            updatePos.move(direction);
            blockstate = level.getBlockState(updatePos);
            if (blockstate.isOf(Blocks.COMPARATOR)) {
                level.updateNeighbor(blockstate, updatePos, provokingBlock, null, false);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (lastKnownPos == null)
            lastKnownPos = getPos();
        else if (!lastKnownPos.equals(pos) && pos != null) {
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
        return controller == null || pos.getX() == controller.getX() && pos.getY() == controller.getY() && pos.getZ() == controller.getZ();
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = pos;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ItemVaultBlockEntity getControllerBE() {
        if (isController())
            return this;
        BlockEntity blockEntity = world.getBlockEntity(controller);
        if (blockEntity instanceof ItemVaultBlockEntity)
            return (ItemVaultBlockEntity) blockEntity;
        return null;
    }

    public void removeController(boolean keepContents) {
        if (world.isClient())
            return;
        updateConnectivity = true;
        controller = null;
        radius = 1;
        length = 1;

        BlockState state = getCachedState();
        if (ItemVaultBlock.isVault(state)) {
            state = state.with(ItemVaultBlock.LARGE, false);
            getWorld().setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.NO_REDRAW | Block.FORCE_STATE);
        }

        itemCapability = null;
        markDirty();
        sendData();
    }

    @Override
    public void setController(BlockPos controller) {
        if (world.isClient && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        itemCapability = null;
        markDirty();
        sendData();
    }

    @Override
    public BlockPos getController() {
        return isController() ? pos : controller;
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = radius;
        int prevLength = length;

        updateConnectivity = view.getBoolean("Uninitialized", false);

        lastKnownPos = view.read("LastKnownPos", BlockPos.CODEC).orElse(null);

        controller = view.read("Controller", BlockPos.CODEC).orElse(null);

        if (isController()) {
            radius = view.getInt("Size", 0);
            length = view.getInt("Length", 0);
        }

        if (!clientPacket) {
            inventory.read(view);
            return;
        }

        boolean changeOfController = controllerBefore == null ? controller != null : !controllerBefore.equals(controller);
        if (hasWorld() && (changeOfController || prevSize != radius || prevLength != length))
            world.scheduleBlockRerenderIfNeeded(getPos(), Blocks.AIR.getDefaultState(), getCachedState());
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        if (updateConnectivity)
            view.putBoolean("Uninitialized", true);

        if (lastKnownPos != null)
            view.put("LastKnownPos", BlockPos.CODEC, lastKnownPos);
        if (isController()) {
            view.putInt("Size", radius);
            view.putInt("Length", length);
        } else {
            view.put("Controller", BlockPos.CODEC, controller);
        }

        super.write(view, clientPacket);

        if (!clientPacket) {
            view.putString("StorageType", "CombinedInv");
            inventory.write(view);
        }
    }

    @Override
    public void clear() {
        inventory.clear();
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
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            inventory.setStack(i, handler.getStack(i));
        }
        for (int i = size, max = inventory.size(); i < max; i++) {
            inventory.setStack(i, ItemStack.EMPTY);
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

        boolean alongZ = ItemVaultBlock.getVaultBlockAxis(getCachedState()) == Axis.Z;
        ItemVaultHandler[] invs = new ItemVaultHandler[length * radius * radius];
        Find:
        for (int yOffset = 0; yOffset < length; yOffset++) {
            for (int xOffset = 0; xOffset < radius; xOffset++) {
                for (int zOffset = 0; zOffset < radius; zOffset++) {
                    BlockPos vaultPos = alongZ ? pos.add(xOffset, zOffset, yOffset) : pos.add(yOffset, xOffset, zOffset);
                    ItemVaultBlockEntity vaultAt = ConnectivityHandler.partAt(AllBlockEntityTypes.ITEM_VAULT, world, vaultPos);
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
        BlockPos farCorner = alongZ ? pos.add(radius, radius, length) : pos.add(length, radius, radius);
        BlockBox bounds = BlockBox.create(this.pos, farCorner);
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
        BlockState state = this.getCachedState();
        if (ItemVaultBlock.isVault(state)) { // safety
            world.setBlockState(getPos(), state.with(ItemVaultBlock.LARGE, radius > 2), Block.NOTIFY_LISTENERS | Block.NO_REDRAW);
        }
        itemCapability = null;
        markDirty();
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
        protected final DefaultedList<ItemStack> stacks;

        public ItemVaultHandler() {
            size = AllConfigs.server().logistics.vaultCapacity.get();
            stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot >= size) {
                return ItemStack.EMPTY;
            }
            return stacks.get(slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot >= size) {
                return;
            }
            stacks.set(slot, stack);
        }

        @Override
        public void markDirty() {
            world.markDirty(pos);
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
        public int size() {
            return size;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot >= size) {
                return ItemStack.EMPTY;
            }
            int i = slot / vaultCapacity;
            accessed.set(i);
            return itemHandler[i].getStack(slot % vaultCapacity);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot >= size) {
                return;
            }
            int i = slot / vaultCapacity;
            ItemVaultHandler handler = itemHandler[i];
            handler.setStack(slot % vaultCapacity, stack);
            handler.markDirty();
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
        public void markDirty() {
            for (ItemVaultHandler inventory : itemHandler) {
                inventory.markDirty();
            }
            incrementVersion();
        }

        private void incrementVersion() {
            updateComparators();
            version++;
        }
    }
}
