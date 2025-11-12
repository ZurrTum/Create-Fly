package com.zurrtum.create.content.logistics.packager;

import com.mojang.serialization.Codec;
import com.zurrtum.create.*;
import com.zurrtum.create.api.packager.unpacking.UnpackingHandler;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.crate.BottomlessItemHandler;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.zurrtum.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.zurrtum.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.zurrtum.create.content.logistics.packagerLink.RequestPromiseQueue;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase.InterfaceProvider;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import com.zurrtum.create.infrastructure.packet.s2c.WiFiEffectPacket;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PackagerBlockEntity extends SmartBlockEntity {
    private static final Codec<List<BigItemStack>> EXITING_CODEC = BigItemStack.CODEC.listOf();

    public boolean redstonePowered;
    public int buttonCooldown;
    public String signBasedAddress;

    public InvManipulationBehaviour targetInventory;
    public ItemStack heldBox;
    public ItemStack previouslyUnwrapped;

    public List<BigItemStack> queuedExitingPackages;

    public final PackagerItemHandler inventory;

    public static final int CYCLE = 20;
    public int animationTicks;
    public boolean animationInward;

    //TODO
    //    public AbstractComputerBehaviour computerBehaviour;
    public Boolean hasCustomComputerAddress;
    public String customComputerAddress;

    private InventorySummary availableItems;
    private VersionedInventoryTrackerBehaviour invVersionTracker;

    //

    public PackagerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        redstonePowered = state.getValueOrElse(PackagerBlock.POWERED, false);
        heldBox = ItemStack.EMPTY;
        previouslyUnwrapped = ItemStack.EMPTY;
        inventory = new PackagerItemHandler(this);
        animationTicks = 0;
        animationInward = true;
        queuedExitingPackages = new LinkedList<>();
        signBasedAddress = "";
        customComputerAddress = "";
        hasCustomComputerAddress = false;
        buttonCooldown = 0;
    }

    public PackagerBlockEntity(BlockPos pos, BlockState state) {
        this(AllBlockEntityTypes.PACKAGER, pos, state);
    }

    //TODO
    //    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    //        if (Mods.COMPUTERCRAFT.isLoaded()) {
    //            event.registerBlockEntity(
    //                PeripheralCapability.get(),
    //                AllBlockEntityTypes.PACKAGER.get(),
    //                (be, context) -> be.computerBehaviour.getPeripheralCapability()
    //            );
    //        }
    //    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(targetInventory = new InvManipulationBehaviour(
            this,
            InterfaceProvider.oppositeOfBlockFacing()
        ).withFilter(this::supportsBlockEntity));
        behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));
        //TODO
        //        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.PACKAGER);
    }

    private boolean supportsBlockEntity(BlockEntity target) {
        return target != null && !(target instanceof PortableStorageInterfaceBlockEntity);
    }

    @Override
    public void initialize() {
        super.initialize();
        recheckIfLinksPresent();
    }

    //TODO
    //    @Override
    //    public void invalidate() {
    //        super.invalidate();
    //        computerBehaviour.removePeripheral();
    //    }

    @Override
    public void tick() {
        super.tick();

        if (buttonCooldown > 0)
            buttonCooldown--;

        if (animationTicks == 0) {
            previouslyUnwrapped = ItemStack.EMPTY;

            if (!level.isClientSide() && !queuedExitingPackages.isEmpty() && heldBox.isEmpty()) {
                BigItemStack entry = queuedExitingPackages.getFirst();
                heldBox = entry.stack.copy();

                entry.count--;
                if (entry.count <= 0)
                    queuedExitingPackages.removeFirst();

                animationInward = false;
                animationTicks = CYCLE;
                notifyUpdate();
            }

            return;
        }

        if (level.isClientSide()) {
            if (animationTicks == CYCLE - (animationInward ? 5 : 1))
                AllSoundEvents.PACKAGER.playAt(level, worldPosition, 1, 1, true);
            if (animationTicks == (animationInward ? 1 : 5))
                level.playLocalSound(worldPosition, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.25f, 0.75f, true);
        }

        animationTicks--;

        if (animationTicks == 0 && !level.isClientSide()) {
            wakeTheFrogs();
            setChanged();
        }
    }

    public void triggerStockCheck() {
        getAvailableItems();
    }

    public InventorySummary getAvailableItems() {
        if (availableItems != null && invVersionTracker.stillWaiting(targetInventory.getInventory()))
            return availableItems;

        InventorySummary availableItems = new InventorySummary();

        Container targetInv = targetInventory.getInventory();
        if (targetInv == null || targetInv instanceof PackagerItemHandler) {
            this.availableItems = availableItems;
            return availableItems;
        }

        if (targetInv instanceof BottomlessItemHandler bih) {
            availableItems.add(bih.getItem(0), BigItemStack.INF);
            this.availableItems = availableItems;
            return availableItems;
        }

        for (int slot = 0, size = targetInv.getContainerSize(); slot < size; slot++) {
            availableItems.add(targetInv.getItem(slot));
        }

        invVersionTracker.awaitNewVersion(targetInventory.getInventory());
        submitNewArrivals(this.availableItems, availableItems);
        this.availableItems = availableItems;
        return availableItems;
    }

    private void submitNewArrivals(InventorySummary before, InventorySummary after) {
        if (before == null || after.isEmpty())
            return;

        Set<RequestPromiseQueue> promiseQueues = new HashSet<>();

        for (Direction d : Iterate.directions) {
            if (!level.isLoaded(worldPosition.relative(d)))
                continue;

            BlockState adjacentState = level.getBlockState(worldPosition.relative(d));
            if (adjacentState.is(AllBlocks.FACTORY_GAUGE)) {
                if (FactoryPanelBlock.connectedDirection(adjacentState) != d)
                    continue;
                if (!(level.getBlockEntity(worldPosition.relative(d)) instanceof FactoryPanelBlockEntity fpbe))
                    continue;
                if (!fpbe.restocker)
                    continue;
                for (ServerFactoryPanelBehaviour behaviour : fpbe.panels.values()) {
                    if (!behaviour.isActive())
                        continue;
                    promiseQueues.add(behaviour.restockerPromises);
                }
            }

            if (adjacentState.is(AllBlocks.STOCK_LINK)) {
                if (PackagerLinkBlock.getConnectedDirection(adjacentState) != d)
                    continue;
                if (!(level.getBlockEntity(worldPosition.relative(d)) instanceof PackagerLinkBlockEntity plbe))
                    continue;
                UUID freqId = plbe.behaviour.freqId;
                if (!Create.LOGISTICS.hasQueuedPromises(freqId))
                    continue;
                promiseQueues.add(Create.LOGISTICS.getQueuedPromises(freqId));
            }
        }

        if (promiseQueues.isEmpty())
            return;

        for (BigItemStack entry : after.getStacks())
            before.add(entry.stack, -entry.count);
        for (RequestPromiseQueue queue : promiseQueues)
            for (BigItemStack entry : before.getStacks())
                if (entry.count < 0)
                    queue.itemEnteredSystem(entry.stack, -entry.count);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level.isClientSide())
            return;
        recheckIfLinksPresent();
        if (!redstonePowered)
            return;
        redstonePowered = getBlockState().getValueOrElse(PackagerBlock.POWERED, false);
        if (!redstoneModeActive())
            return;
        updateSignAddress();
        attemptToSend(null);
    }

    public void recheckIfLinksPresent() {
        if (level.isClientSide())
            return;
        BlockState blockState = getBlockState();
        if (!blockState.hasProperty(PackagerBlock.LINKED))
            return;
        boolean shouldBeLinked = getLinkPos() != null;
        boolean isLinked = blockState.getValue(PackagerBlock.LINKED);
        if (shouldBeLinked == isLinked)
            return;
        level.setBlockAndUpdate(worldPosition, blockState.cycle(PackagerBlock.LINKED));
    }

    public boolean redstoneModeActive() {
        return !getBlockState().getValueOrElse(PackagerBlock.LINKED, false);
    }

    private BlockPos getLinkPos() {
        for (Direction d : Iterate.directions) {
            BlockState adjacentState = level.getBlockState(worldPosition.relative(d));
            if (!adjacentState.is(AllBlocks.STOCK_LINK))
                continue;
            if (PackagerLinkBlock.getConnectedDirection(adjacentState) != d)
                continue;
            return worldPosition.relative(d);
        }
        return null;
    }

    public void flashLink() {
        if (!(level instanceof ServerLevel serverWorld)) {
            return;
        }
        for (Direction d : Iterate.directions) {
            BlockPos adjacentPos = worldPosition.relative(d);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (!adjacentState.is(AllBlocks.STOCK_LINK))
                continue;
            if (PackagerLinkBlock.getConnectedDirection(adjacentState) != d)
                continue;
            serverWorld.getServer().getPlayerList().broadcast(
                null,
                adjacentPos.getX(),
                adjacentPos.getY(),
                adjacentPos.getZ(),
                32,
                serverWorld.dimension(),
                new WiFiEffectPacket(adjacentPos)
            );
            return;
        }
    }

    public boolean isTooBusyFor(RequestType type) {
        int queue = queuedExitingPackages.size();
        return queue >= switch (type) {
            case PLAYER -> 50;
            case REDSTONE -> 20;
            case RESTOCK -> 10;
        };
    }

    public void activate() {
        redstonePowered = true;
        setChanged();

        recheckIfLinksPresent();
        if (!redstoneModeActive())
            return;

        updateSignAddress();
        attemptToSend(null);

        // dont send multiple packages when a button signal length is received
        if (buttonCooldown <= 0) { // still on button cooldown, don't prolong it
            buttonCooldown = 40;
        }
    }

    public boolean unwrapBox(ItemStack box, boolean simulate) {
        if (animationTicks > 0)
            return false;

        Objects.requireNonNull(level);

        ItemStackHandler contents = PackageItem.getContents(box);
        List<ItemStack> items = ItemHelper.getNonEmptyStacks(contents);
        if (items.isEmpty())
            return true;

        PackageOrderWithCrafts orderContext = PackageItem.getOrderContext(box);
        Direction facing = getBlockState().getValueOrElse(PackagerBlock.FACING, Direction.UP);
        BlockPos target = worldPosition.relative(facing.getOpposite());
        BlockState targetState = level.getBlockState(target);

        UnpackingHandler handler = UnpackingHandler.REGISTRY.get(targetState);
        UnpackingHandler toUse = handler != null ? handler : AllUnpackingHandlers.DEFAULT;
        // note: handler may modify the passed items
        boolean unpacked = toUse.unpack(level, target, targetState, facing, items, orderContext, simulate);

        if (unpacked && !simulate) {
            previouslyUnwrapped = box;
            animationInward = true;
            animationTicks = CYCLE;
            notifyUpdate();
        }

        return unpacked;
    }

    public void attemptToSend(@Nullable List<PackagingRequest> queuedRequests) {
        if (queuedRequests == null && (!heldBox.isEmpty() || animationTicks != 0 || buttonCooldown > 0))
            return;

        Container targetInv = targetInventory.getInventory();
        if (targetInv == null || targetInv instanceof PackagerItemHandler)
            return;

        MutableBoolean anyItemPresent = new MutableBoolean();
        ItemStackHandler extractedItems = new ItemStackHandler(PackageItem.SLOTS);
        ItemStack extractedPackageItem = ItemStack.EMPTY;
        PackagingRequest nextRequest = null;
        String fixedAddress = null;
        int fixedOrderId = 0;

        // Data written to packages for defrags
        int linkIndexInOrder = 0;
        boolean finalLinkInOrder = false;
        int packageIndexAtLink = 0;
        boolean finalPackageAtLink = false;
        PackageOrderWithCrafts orderContext = null;
        boolean requestQueue = queuedRequests != null;

        if (requestQueue && !queuedRequests.isEmpty()) {
            nextRequest = queuedRequests.get(0);
            fixedAddress = nextRequest.address();
            fixedOrderId = nextRequest.orderId();
            linkIndexInOrder = nextRequest.linkIndex();
            finalLinkInOrder = nextRequest.finalLink().booleanValue();
            packageIndexAtLink = nextRequest.packageCounter().getAndIncrement();
            orderContext = nextRequest.context();
        }

        Outer:
        for (int i = 0; i < PackageItem.SLOTS; i++) {
            boolean continuePacking = true;

            while (continuePacking) {
                continuePacking = false;

                if (requestQueue) {
                    ItemStack stack = nextRequest.item();
                    Item item = stack.getItem();

                    boolean bulky = !item.canFitInsideContainerItems();
                    if (bulky && anyItemPresent.isTrue())
                        break Outer;

                    int count = Math.min(64, nextRequest.getCount());
                    int extract = targetInv.extract(stack, count);
                    if (extract == 0)
                        break Outer;
                    anyItemPresent.setTrue();
                    extractedItems.insert(stack, extract);

                    if (item instanceof PackageItem)
                        extractedPackageItem = stack.copyWithCount(extract);

                    nextRequest.subtract(extract);

                    if (!nextRequest.isEmpty()) {
                        if (bulky)
                            break Outer;
                        break;
                    }

                    finalPackageAtLink = true;
                    queuedRequests.removeFirst();
                    if (queuedRequests.isEmpty())
                        break Outer;
                    int previousCount = nextRequest.packageCounter().intValue();
                    nextRequest = queuedRequests.getFirst();
                    if (!fixedAddress.equals(nextRequest.address()))
                        break Outer;
                    if (fixedOrderId != nextRequest.orderId())
                        break Outer;

                    nextRequest.packageCounter().setValue(previousCount);
                    finalPackageAtLink = false;
                    continuePacking = true;
                    if (nextRequest.context() != null)
                        orderContext = nextRequest.context();

                    if (bulky)
                        break Outer;
                } else {
                    ItemStack extracted = targetInv.extract(stack -> anyItemPresent.isFalse() || stack.getItem().canFitInsideContainerItems(), 64);
                    if (extracted.isEmpty())
                        break Outer;

                    anyItemPresent.setTrue();
                    extractedItems.insert(extracted);
                    Item item = extracted.getItem();

                    if (item instanceof PackageItem)
                        extractedPackageItem = extracted;
                    if (!item.canFitInsideContainerItems())
                        break Outer;
                }
            }
        }

        if (anyItemPresent.isFalse()) {
            if (nextRequest != null)
                queuedRequests.removeFirst();
            return;
        }

        ItemStack createdBox = extractedPackageItem.isEmpty() ? PackageItem.containing(extractedItems) : extractedPackageItem;
        PackageItem.clearAddress(createdBox);

        if (fixedAddress != null && !fixedAddress.isBlank())
            PackageItem.addAddress(createdBox, fixedAddress);
        if (requestQueue)
            PackageItem.setOrder(createdBox, fixedOrderId, linkIndexInOrder, finalLinkInOrder, packageIndexAtLink, finalPackageAtLink, orderContext);
        if (!requestQueue && !signBasedAddress.isBlank())
            PackageItem.addAddress(createdBox, signBasedAddress);

        BlockPos linkPos = getLinkPos();
        if (extractedPackageItem.isEmpty() && linkPos != null && level.getBlockEntity(linkPos) instanceof PackagerLinkBlockEntity plbe)
            plbe.behaviour.deductFromAccurateSummary(extractedItems);

        if (!heldBox.isEmpty() || animationTicks != 0) {
            queuedExitingPackages.add(new BigItemStack(createdBox, 1));
            return;
        }

        heldBox = createdBox;
        animationInward = false;
        animationTicks = CYCLE;

        award(AllAdvancements.PACKAGER);
        triggerStockCheck();
        notifyUpdate();
    }

    public void updateSignAddress() {
        signBasedAddress = "";
        for (Direction side : Iterate.directions) {
            String address = getSign(side);
            if (address == null || address.isBlank())
                continue;
            signBasedAddress = address;
        }
        //TODO
        //        if (computerBehaviour.hasAttachedComputer() && hasCustomComputerAddress) {
        //            signBasedAddress = customComputerAddress;
        //        } else {
        hasCustomComputerAddress = false;
        //        }
    }

    protected String getSign(Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(side));
        if (!(blockEntity instanceof SignBlockEntity sign))
            return null;
        for (boolean front : Iterate.trueAndFalse) {
            SignText text = sign.getText(front);
            String address = "";
            for (Component component : text.getMessages(false)) {
                String string = component.getString();
                if (!string.isBlank())
                    address += string.trim() + " ";
            }
            if (!address.isBlank())
                return address.trim();
        }
        return null;
    }

    protected void wakeTheFrogs() {
        //TODO
        //        if (world.getBlockEntity(pos.offset(Direction.UP)) instanceof FrogportBlockEntity port)
        //            port.tryPullingFromOwnAndAdjacentInventories();
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        redstonePowered = view.getBooleanOr("Active", false);
        animationInward = view.getBooleanOr("AnimationInward", false);
        animationTicks = view.getIntOr("AnimationTicks", 0);
        signBasedAddress = view.getStringOr("SignAddress", "");
        customComputerAddress = view.getStringOr("ComputerAddress", "");
        hasCustomComputerAddress = view.getBooleanOr("HasComputerAddress", false);
        heldBox = view.read("HeldBox", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        previouslyUnwrapped = view.read("InsertedBox", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (clientPacket)
            return;
        queuedExitingPackages.clear();
        view.read("QueuedExitingPackages", EXITING_CODEC).ifPresent(list -> queuedExitingPackages.addAll(list));
        view.read("LastSummary", InventorySummary.CODEC).ifPresent(summary -> availableItems = summary);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putBoolean("Active", redstonePowered);
        view.putBoolean("AnimationInward", animationInward);
        view.putInt("AnimationTicks", animationTicks);
        view.putString("SignAddress", signBasedAddress);
        view.putString("ComputerAddress", customComputerAddress);
        view.putBoolean("HasComputerAddress", hasCustomComputerAddress);
        if (!heldBox.isEmpty()) {
            view.store("HeldBox", ItemStack.CODEC, heldBox);
        }
        if (!previouslyUnwrapped.isEmpty()) {
            view.store("InsertedBox", ItemStack.CODEC, previouslyUnwrapped);
        }
        if (clientPacket)
            return;
        view.store("QueuedExitingPackages", EXITING_CODEC, queuedExitingPackages);
        if (availableItems != null)
            view.store("LastSummary", InventorySummary.CODEC, availableItems);
    }

    @Override
    public void destroy() {
        super.destroy();
        Containers.dropContents(level, worldPosition, inventory);
        queuedExitingPackages.forEach(bigStack -> {
            for (int i = 0; i < bigStack.count; i++)
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), bigStack.stack.copy());
        });
        queuedExitingPackages.clear();
    }

    public float getTrayOffset(float partialTicks) {
        float tickCycle = animationInward ? animationTicks - partialTicks : animationTicks - 5 - partialTicks;
        float progress = Mth.clamp(tickCycle / (CYCLE - 5) * 2 - 1, -1, 1);
        progress = 1 - progress * progress;
        return progress * progress;
    }

    public ItemStack getRenderedBox() {
        if (animationInward)
            return animationTicks <= CYCLE / 2 ? ItemStack.EMPTY : previouslyUnwrapped;
        return animationTicks >= CYCLE / 2 ? ItemStack.EMPTY : heldBox;
    }

    public boolean isTargetingSameInventory(@Nullable IdentifiedInventory inventory) {
        if (inventory == null)
            return false;

        Container targetHandler = targetInventory.getInventory();
        if (targetHandler == null)
            return false;

        if (inventory.identifier() != null) {
            BlockFace face = targetInventory.getTarget().getOpposite();
            return inventory.identifier().contains(face);
        } else {
            return isSameInventoryFallback(targetHandler, inventory.handler());
        }
    }

    private static boolean isSameInventoryFallback(Container first, Container second) {
        if (first == second)
            return true;

        // If a contained ItemStack instance is the same, we can be pretty sure these
        // inventories are the same (works for compound inventories)
        for (int i = 0, secondSize = second.getContainerSize(); i < secondSize; i++) {
            ItemStack stackInSlot = second.getItem(i);
            if (stackInSlot.isEmpty())
                continue;
            for (int j = 0, firstSize = first.getContainerSize(); j < firstSize; j++)
                if (stackInSlot == first.getItem(j))
                    return true;
            break;
        }

        return false;
    }
}
