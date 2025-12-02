package com.zurrtum.create.content.logistics.packager;

import com.mojang.serialization.Codec;
import com.zurrtum.create.*;
import com.zurrtum.create.api.packager.unpacking.UnpackingHandler;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.compat.computercraft.events.PackageEvent;
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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    public AbstractComputerBehaviour computerBehaviour;
    public Boolean hasCustomComputerAddress;
    public String customComputerAddress;

    private InventorySummary availableItems;
    private VersionedInventoryTrackerBehaviour invVersionTracker;

    public PackagerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        redstonePowered = state.get(PackagerBlock.POWERED, false);
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

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(targetInventory = new InvManipulationBehaviour(
            this,
            InterfaceProvider.oppositeOfBlockFacing()
        ).withFilter(this::supportsBlockEntity));
        behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
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

    @Override
    public void tick() {
        super.tick();

        if (buttonCooldown > 0)
            buttonCooldown--;

        if (animationTicks == 0) {
            previouslyUnwrapped = ItemStack.EMPTY;

            if (!world.isClient() && !queuedExitingPackages.isEmpty() && heldBox.isEmpty()) {
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

        if (world.isClient()) {
            if (animationTicks == CYCLE - (animationInward ? 5 : 1))
                AllSoundEvents.PACKAGER.playAt(world, pos, 1, 1, true);
            if (animationTicks == (animationInward ? 1 : 5))
                world.playSoundAtBlockCenterClient(pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 0.25f, 0.75f, true);
        }

        animationTicks--;

        if (animationTicks == 0 && !world.isClient()) {
            wakeTheFrogs();
            markDirty();
        }
    }

    public void triggerStockCheck() {
        getAvailableItems();
    }

    public InventorySummary getAvailableItems() {
        if (availableItems != null && invVersionTracker.stillWaiting(targetInventory.getInventory()))
            return availableItems;

        InventorySummary availableItems = new InventorySummary();

        Inventory targetInv = targetInventory.getInventory();
        if (targetInv == null || targetInv instanceof PackagerItemHandler) {
            this.availableItems = availableItems;
            return availableItems;
        }

        if (targetInv instanceof BottomlessItemHandler bih) {
            availableItems.add(bih.getStack(0), BigItemStack.INF);
            this.availableItems = availableItems;
            return availableItems;
        }

        for (int slot = 0, size = targetInv.size(); slot < size; slot++) {
            availableItems.add(targetInv.getStack(slot));
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
            if (!world.isPosLoaded(pos.offset(d)))
                continue;

            BlockState adjacentState = world.getBlockState(pos.offset(d));
            if (adjacentState.isOf(AllBlocks.FACTORY_GAUGE)) {
                if (FactoryPanelBlock.connectedDirection(adjacentState) != d)
                    continue;
                if (!(world.getBlockEntity(pos.offset(d)) instanceof FactoryPanelBlockEntity fpbe))
                    continue;
                if (!fpbe.restocker)
                    continue;
                for (ServerFactoryPanelBehaviour behaviour : fpbe.panels.values()) {
                    if (!behaviour.isActive())
                        continue;
                    promiseQueues.add(behaviour.restockerPromises);
                }
            }

            if (adjacentState.isOf(AllBlocks.STOCK_LINK)) {
                if (PackagerLinkBlock.getConnectedDirection(adjacentState) != d)
                    continue;
                if (!(world.getBlockEntity(pos.offset(d)) instanceof PackagerLinkBlockEntity plbe))
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
        if (world.isClient())
            return;
        recheckIfLinksPresent();
        if (!redstonePowered)
            return;
        redstonePowered = getCachedState().get(PackagerBlock.POWERED, false);
        if (!redstoneModeActive())
            return;
        updateSignAddress();
        attemptToSend(null);
    }

    public void recheckIfLinksPresent() {
        if (world.isClient())
            return;
        BlockState blockState = getCachedState();
        if (!blockState.contains(PackagerBlock.LINKED))
            return;
        boolean shouldBeLinked = getLinkPos() != null;
        boolean isLinked = blockState.get(PackagerBlock.LINKED);
        if (shouldBeLinked == isLinked)
            return;
        world.setBlockState(pos, blockState.cycle(PackagerBlock.LINKED));
    }

    public boolean redstoneModeActive() {
        return !getCachedState().get(PackagerBlock.LINKED, false);
    }

    private BlockPos getLinkPos() {
        for (Direction d : Iterate.directions) {
            BlockState adjacentState = world.getBlockState(pos.offset(d));
            if (!adjacentState.isOf(AllBlocks.STOCK_LINK))
                continue;
            if (PackagerLinkBlock.getConnectedDirection(adjacentState) != d)
                continue;
            return pos.offset(d);
        }
        return null;
    }

    public void flashLink() {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        for (Direction d : Iterate.directions) {
            BlockPos adjacentPos = pos.offset(d);
            BlockState adjacentState = world.getBlockState(adjacentPos);
            if (!adjacentState.isOf(AllBlocks.STOCK_LINK))
                continue;
            if (PackagerLinkBlock.getConnectedDirection(adjacentState) != d)
                continue;
            serverWorld.getServer().getPlayerManager().sendToAround(
                null,
                adjacentPos.getX(),
                adjacentPos.getY(),
                adjacentPos.getZ(),
                32,
                serverWorld.getRegistryKey(),
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
        markDirty();

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

        Objects.requireNonNull(world);

        ItemStackHandler contents = PackageItem.getContents(box);
        List<ItemStack> items = ItemHelper.getNonEmptyStacks(contents);
        if (items.isEmpty())
            return true;

        PackageOrderWithCrafts orderContext = PackageItem.getOrderContext(box);
        Direction facing = getCachedState().get(PackagerBlock.FACING, Direction.UP);
        BlockPos target = pos.offset(facing.getOpposite());
        BlockState targetState = world.getBlockState(target);

        UnpackingHandler handler = UnpackingHandler.REGISTRY.get(targetState);
        UnpackingHandler toUse = handler != null ? handler : AllUnpackingHandlers.DEFAULT;
        // note: handler may modify the passed items
        boolean unpacked = toUse.unpack(world, target, targetState, facing, items, orderContext, simulate);

        if (unpacked && !simulate) {
            computerBehaviour.prepareComputerEvent(new PackageEvent(box, "package_received"));
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

        Inventory targetInv = targetInventory.getInventory();
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

                    boolean bulky = !item.canBeNested();
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
                    ItemStack extracted = targetInv.extract(stack -> anyItemPresent.isFalse() || stack.getItem().canBeNested(), 64);
                    if (extracted.isEmpty())
                        break Outer;

                    anyItemPresent.setTrue();
                    extractedItems.insert(extracted);
                    Item item = extracted.getItem();

                    if (item instanceof PackageItem)
                        extractedPackageItem = extracted;
                    if (!item.canBeNested())
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
        computerBehaviour.prepareComputerEvent(new PackageEvent(createdBox, "package_created"));
        PackageItem.clearAddress(createdBox);

        if (fixedAddress != null && !fixedAddress.isBlank())
            PackageItem.addAddress(createdBox, fixedAddress);
        if (requestQueue)
            PackageItem.setOrder(createdBox, fixedOrderId, linkIndexInOrder, finalLinkInOrder, packageIndexAtLink, finalPackageAtLink, orderContext);
        if (!requestQueue && !signBasedAddress.isBlank())
            PackageItem.addAddress(createdBox, signBasedAddress);

        BlockPos linkPos = getLinkPos();
        if (extractedPackageItem.isEmpty() && linkPos != null && world.getBlockEntity(linkPos) instanceof PackagerLinkBlockEntity plbe)
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
        if (computerBehaviour.hasAttachedComputer() && hasCustomComputerAddress) {
            signBasedAddress = customComputerAddress;
        } else {
            hasCustomComputerAddress = false;
        }
    }

    protected String getSign(Direction side) {
        BlockEntity blockEntity = world.getBlockEntity(pos.offset(side));
        if (!(blockEntity instanceof SignBlockEntity sign))
            return null;
        for (boolean front : Iterate.trueAndFalse) {
            SignText text = sign.getText(front);
            String address = "";
            for (Text component : text.getMessages(false)) {
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
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        redstonePowered = view.getBoolean("Active", false);
        animationInward = view.getBoolean("AnimationInward", false);
        animationTicks = view.getInt("AnimationTicks", 0);
        signBasedAddress = view.getString("SignAddress", "");
        customComputerAddress = view.getString("ComputerAddress", "");
        hasCustomComputerAddress = view.getBoolean("HasComputerAddress", false);
        heldBox = view.read("HeldBox", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        previouslyUnwrapped = view.read("InsertedBox", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (clientPacket)
            return;
        queuedExitingPackages.clear();
        view.read("QueuedExitingPackages", EXITING_CODEC).ifPresent(list -> queuedExitingPackages.addAll(list));
        view.read("LastSummary", InventorySummary.CODEC).ifPresent(summary -> availableItems = summary);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putBoolean("Active", redstonePowered);
        view.putBoolean("AnimationInward", animationInward);
        view.putInt("AnimationTicks", animationTicks);
        view.putString("SignAddress", signBasedAddress);
        view.putString("ComputerAddress", customComputerAddress);
        view.putBoolean("HasComputerAddress", hasCustomComputerAddress);
        if (!heldBox.isEmpty()) {
            view.put("HeldBox", ItemStack.CODEC, heldBox);
        }
        if (!previouslyUnwrapped.isEmpty()) {
            view.put("InsertedBox", ItemStack.CODEC, previouslyUnwrapped);
        }
        if (clientPacket)
            return;
        view.put("QueuedExitingPackages", EXITING_CODEC, queuedExitingPackages);
        if (availableItems != null)
            view.put("LastSummary", InventorySummary.CODEC, availableItems);
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemScatterer.spawn(world, pos, inventory);
        queuedExitingPackages.forEach(bigStack -> {
            for (int i = 0; i < bigStack.count; i++)
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), bigStack.stack.copy());
        });
        queuedExitingPackages.clear();
    }

    public float getTrayOffset(float partialTicks) {
        float tickCycle = animationInward ? animationTicks - partialTicks : animationTicks - 5 - partialTicks;
        float progress = MathHelper.clamp(tickCycle / (CYCLE - 5) * 2 - 1, -1, 1);
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

        Inventory targetHandler = targetInventory.getInventory();
        if (targetHandler == null)
            return false;

        if (inventory.identifier() != null) {
            BlockFace face = targetInventory.getTarget().getOpposite();
            return inventory.identifier().contains(face);
        } else {
            return isSameInventoryFallback(targetHandler, inventory.handler());
        }
    }

    private static boolean isSameInventoryFallback(Inventory first, Inventory second) {
        if (first == second)
            return true;

        // If a contained ItemStack instance is the same, we can be pretty sure these
        // inventories are the same (works for compound inventories)
        for (int i = 0, secondSize = second.size(); i < secondSize; i++) {
            ItemStack stackInSlot = second.getStack(i);
            if (stackInSlot.isEmpty())
                continue;
            for (int j = 0, firstSize = first.size(); j < firstSize; j++)
                if (stackInSlot == first.getStack(j))
                    return true;
            break;
        }

        return false;
    }
}
