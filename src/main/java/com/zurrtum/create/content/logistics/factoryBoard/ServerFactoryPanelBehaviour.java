package com.zurrtum.create.content.logistics.factoryBoard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.zurrtum.create.*;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.codecs.CatnipCodecs;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagingRequest;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.zurrtum.create.content.logistics.packagerLink.LogisticsManager;
import com.zurrtum.create.content.logistics.packagerLink.RequestPromise;
import com.zurrtum.create.content.logistics.packagerLink.RequestPromiseQueue;
import com.zurrtum.create.content.logistics.stockTicker.PackageOrder;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.s2c.FactoryPanelEffectPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Math;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

public class ServerFactoryPanelBehaviour extends ServerFilteringBehaviour implements MenuProvider {
    private static final Codec<Set<FactoryPanelPosition>> TARGET_CODEC = CatnipCodecs.set(FactoryPanelPosition.CODEC);
    private static final Codec<List<BigItemStack>> CRAFTING_LIST_CODEC = BigItemStack.CODEC.listOf();

    public static final BehaviourType<ServerFactoryPanelBehaviour> TOP_LEFT = new BehaviourType<>();
    public static final BehaviourType<ServerFactoryPanelBehaviour> TOP_RIGHT = new BehaviourType<>();
    public static final BehaviourType<ServerFactoryPanelBehaviour> BOTTOM_LEFT = new BehaviourType<>();
    public static final BehaviourType<ServerFactoryPanelBehaviour> BOTTOM_RIGHT = new BehaviourType<>();

    public Map<FactoryPanelPosition, FactoryPanelConnection> targetedBy;
    public Map<BlockPos, FactoryPanelConnection> targetedByLinks;
    public Set<FactoryPanelPosition> targeting;
    public List<ItemStack> activeCraftingArrangement;
    public @Nullable List<BigItemStack> craftingList;

    public boolean satisfied;
    public boolean promisedSatisfied;
    public boolean waitingForNetwork;
    public String recipeAddress;
    public int recipeOutput;
    public LerpedFloat bulb;
    public PanelSlot slot;
    public int promiseClearingInterval;
    public boolean forceClearPromises;
    public UUID network;
    public boolean active;

    public boolean redstonePowered;

    public RequestPromiseQueue restockerPromises;
    private boolean promisePrimedForMarkDirty;

    private int lastReportedUnloadedLinks;
    private int lastReportedLevelInStorage;
    private int lastReportedPromises;
    private int timer;

    public ServerFactoryPanelBehaviour(FactoryPanelBlockEntity be, PanelSlot slot) {
        super(be);
        this.slot = slot;
        this.targetedBy = new HashMap<>();
        this.targetedByLinks = new HashMap<>();
        this.targeting = new HashSet<>();
        this.count = 0;
        this.satisfied = false;
        this.promisedSatisfied = false;
        this.waitingForNetwork = false;
        this.activeCraftingArrangement = List.of();
        this.recipeAddress = "";
        this.recipeOutput = 1;
        this.active = false;
        this.forceClearPromises = false;
        this.redstonePowered = false;
        this.promiseClearingInterval = -1;
        this.bulb = LerpedFloat.linear().startWithValue(0).chase(0, 0.175, Chaser.EXP);
        this.restockerPromises = new RequestPromiseQueue(be::setChanged);
        this.promisePrimedForMarkDirty = true;
        this.network = UUID.randomUUID();
        setLazyTickRate(40);
    }

    public void setNetwork(UUID network) {
        this.network = network;
    }

    @Nullable
    public static ServerFactoryPanelBehaviour at(BlockAndTintGetter world, FactoryPanelConnection connection) {
        Object cached = connection.cachedSource.get();
        if (cached instanceof ServerFactoryPanelBehaviour fbe && !fbe.blockEntity.isRemoved())
            return fbe;
        ServerFactoryPanelBehaviour result = at(world, connection.from);
        connection.cachedSource = new WeakReference<>(result);
        return result;
    }

    @Nullable
    public static ServerFactoryPanelBehaviour at(BlockAndTintGetter world, FactoryPanelPosition pos) {
        if (world instanceof Level l && !l.isLoaded(pos.pos()))
            return null;
        if (!(world.getBlockEntity(pos.pos()) instanceof FactoryPanelBlockEntity fpbe))
            return null;
        ServerFactoryPanelBehaviour behaviour = fpbe.panels.get(pos.slot());
        if (!behaviour.active)
            return null;
        return behaviour;
    }

    @Nullable
    public static FactoryPanelSupportBehaviour linkAt(BlockAndTintGetter world, FactoryPanelConnection connection) {
        Object cached = connection.cachedSource.get();
        if (cached instanceof FactoryPanelSupportBehaviour fpsb && !fpsb.blockEntity.isRemoved())
            return fpsb;
        FactoryPanelSupportBehaviour result = linkAt(world, connection.from);
        connection.cachedSource = new WeakReference<>(result);
        return result;
    }

    @Nullable
    public static FactoryPanelSupportBehaviour linkAt(BlockAndTintGetter world, FactoryPanelPosition pos) {
        if (world instanceof Level l && !l.isLoaded(pos.pos()))
            return null;
        return BlockEntityBehaviour.get(world, pos.pos(), FactoryPanelSupportBehaviour.TYPE);
    }

    public void moveTo(FactoryPanelPosition newPos, ServerPlayer player) {
        Level level = getLevel();
        BlockState existingState = level.getBlockState(newPos.pos());

        // Check if target pos is valid
        if (ServerFactoryPanelBehaviour.at(level, newPos) != null)
            return;
        boolean isAddedToOtherGauge = existingState.is(AllBlocks.FACTORY_GAUGE);
        if (!existingState.isAir() && !isAddedToOtherGauge)
            return;
        if (isAddedToOtherGauge && existingState != blockEntity.getBlockState())
            return;
        if (!isAddedToOtherGauge)
            level.setBlock(newPos.pos(), blockEntity.getBlockState(), Block.UPDATE_ALL);

        for (BlockPos blockPos : targetedByLinks.keySet())
            if (!blockPos.closerThan(newPos.pos(), 24))
                return;
        for (FactoryPanelPosition blockPos : targetedBy.keySet())
            if (!blockPos.pos().closerThan(newPos.pos(), 24))
                return;
        for (FactoryPanelPosition blockPos : targeting)
            if (!blockPos.pos().closerThan(newPos.pos(), 24))
                return;

        // Disconnect links
        for (BlockPos pos : targetedByLinks.keySet()) {
            FactoryPanelSupportBehaviour at = linkAt(level, new FactoryPanelPosition(pos, slot));
            if (at != null)
                at.disconnect(this);
        }

        SmartBlockEntity oldBE = blockEntity;
        FactoryPanelPosition oldPos = getPanelPosition();
        moveToSlot(newPos.slot());

        // Add to new BE
        if (level.getBlockEntity(newPos.pos()) instanceof FactoryPanelBlockEntity fpbe) {
            fpbe.attachBehaviourLate(this);
            fpbe.panels.put(slot, this);
            fpbe.redraw = true;
            fpbe.lastShape = null;
            fpbe.notifyUpdate();
        }

        // Remove from old BE
        if (oldBE instanceof FactoryPanelBlockEntity fpbe) {
            ServerFactoryPanelBehaviour newBehaviour = new ServerFactoryPanelBehaviour(fpbe, oldPos.slot());
            fpbe.attachBehaviourLate(newBehaviour);
            fpbe.panels.put(oldPos.slot(), newBehaviour);
            fpbe.redraw = true;
            fpbe.lastShape = null;
            fpbe.notifyUpdate();
        }

        // Rewire connections
        for (FactoryPanelPosition position : targeting) {
            ServerFactoryPanelBehaviour at = at(level, position);
            if (at != null) {
                FactoryPanelConnection connection = at.targetedBy.remove(oldPos);
                connection.from = newPos;
                at.targetedBy.put(newPos, connection);
                at.blockEntity.sendData();
            }
        }

        for (FactoryPanelPosition position : targetedBy.keySet()) {
            ServerFactoryPanelBehaviour at = at(level, position);
            if (at != null) {
                at.targeting.remove(oldPos);
                at.targeting.add(newPos);
            }
        }

        // Reconnect links
        for (BlockPos pos : targetedByLinks.keySet()) {
            FactoryPanelSupportBehaviour at = linkAt(level, new FactoryPanelPosition(pos, slot));
            if (at != null)
                at.connect(this);
        }

        // Tell player
        player.displayClientMessage(Component.translatable("create.factory_panel.relocated").withStyle(ChatFormatting.GREEN), true);
        player.level().playSound(null, newPos.pos(), SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void moveToSlot(PanelSlot slot) {
        this.slot = slot;
        if (getLevel().isClientSide()) {
            AllClientHandle.INSTANCE.factoryPanelMoveToSlot(blockEntity, slot);
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        notifyRedstoneOutputs();
    }

    @Override
    public void tick() {
        super.tick();
        if (getLevel().isClientSide()) {
            if (blockEntity.isVirtual())
                tickStorageMonitor();
            bulb.updateChaseTarget(redstonePowered || satisfied ? 1 : 0);
            bulb.tickChaser();
            return;
        }

        if (!promisePrimedForMarkDirty) {
            restockerPromises.setOnChanged(blockEntity::setChanged);
            promisePrimedForMarkDirty = true;
        }

        tickStorageMonitor();
        tickRequests();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (getLevel().isClientSide())
            return;
        checkForRedstoneInput();
    }

    public void checkForRedstoneInput() {
        if (!active)
            return;

        boolean shouldPower = false;
        for (FactoryPanelConnection connection : targetedByLinks.values()) {
            if (!getLevel().isLoaded(connection.from.pos()))
                return;
            FactoryPanelSupportBehaviour linkAt = linkAt(getLevel(), connection);
            if (linkAt == null)
                return;
            shouldPower |= linkAt.shouldPanelBePowered();
        }

        if (shouldPower == redstonePowered)
            return;

        redstonePowered = shouldPower;
        blockEntity.notifyUpdate();
        timer = 1;
    }

    private void notifyRedstoneOutputs() {
        for (FactoryPanelConnection connection : targetedByLinks.values()) {
            if (!getLevel().isLoaded(connection.from.pos()))
                return;
            FactoryPanelSupportBehaviour linkAt = linkAt(getLevel(), connection);
            if (linkAt == null || linkAt.isOutput())
                return;
            linkAt.notifyLink();
        }
    }

    private void tickStorageMonitor() {
        ItemStack filter = getFilter();
        int unloadedLinkCount = getUnloadedLinks();
        FactoryPanelBlockEntity panelBE = panelBE();
        if (!panelBE.restocker && unloadedLinkCount == 0 && lastReportedUnloadedLinks != 0) {
            // All links have been loaded, invalidate cache so we can get an accurate summary!
            // Otherwise, we will have to wait for 20 ticks and unnecessary packages will be sent!
            LogisticsManager.SUMMARIES.invalidate(network);
        }
        int inStorage = getLevelInStorage();
        int promised = getPromised();
        int demand = getAmount() * (upTo ? 1 : filter.getMaxStackSize());
        boolean shouldSatisfy = filter.isEmpty() || inStorage >= demand;
        boolean shouldPromiseSatisfy = filter.isEmpty() || inStorage + promised >= demand;
        boolean shouldWait = unloadedLinkCount > 0;

        if (lastReportedLevelInStorage == inStorage && lastReportedPromises == promised && lastReportedUnloadedLinks == unloadedLinkCount && satisfied == shouldSatisfy && promisedSatisfied == shouldPromiseSatisfy && waitingForNetwork == shouldWait)
            return;

        if (!satisfied && shouldSatisfy && demand > 0) {
            AllSoundEvents.CONFIRM.playOnServer(getLevel(), getPos(), 0.075f, 1f);
            AllSoundEvents.CONFIRM_2.playOnServer(getLevel(), getPos(), 0.125f, 0.575f);
        }

        boolean notifyOutputs = satisfied != shouldSatisfy;
        lastReportedLevelInStorage = inStorage;
        satisfied = shouldSatisfy;
        lastReportedPromises = promised;
        promisedSatisfied = shouldPromiseSatisfy;
        lastReportedUnloadedLinks = unloadedLinkCount;
        waitingForNetwork = shouldWait;
        if (!getLevel().isClientSide())
            blockEntity.sendData();
        if (notifyOutputs)
            notifyRedstoneOutputs();
    }

    public static class ItemStackConnections extends ArrayList<FactoryPanelConnection> {
        public ItemStack item;
        public int totalAmount;

        public ItemStackConnections(ItemStack item) {
            this.item = item;
        }
    }

    private void tickRequests() {
        FactoryPanelBlockEntity panelBE = panelBE();
        if (targetedBy.isEmpty() && !panelBE.restocker)
            return;
        if (panelBE.restocker)
            restockerPromises.tick();
        if (satisfied || promisedSatisfied || waitingForNetwork || redstonePowered)
            return;
        if (timer > 0) {
            timer = Math.min(timer, getConfigRequestIntervalInTicks());
            timer--;
            return;
        }

        resetTimer();

        if (recipeAddress.isBlank())
            return;

        if (panelBE.restocker) {
            tryRestock();
            return;
        }

        boolean failed = false;

        Map<UUID, Map<ItemStack, ItemStackConnections>> consolidated = new HashMap<>();

        for (FactoryPanelConnection connection : targetedBy.values()) {
            ServerFactoryPanelBehaviour source = at(getLevel(), connection);
            if (source == null)
                return;

            ItemStack item = source.getFilter();

            Map<ItemStack, ItemStackConnections> networkItemCounts = consolidated.computeIfAbsent(
                source.network,
                $ -> new Object2ObjectOpenCustomHashMap<>(ItemStackLinkedSet.TYPE_AND_TAG)
            );
            networkItemCounts.computeIfAbsent(item, $ -> new ItemStackConnections(item));
            ItemStackConnections existingConnections = networkItemCounts.get(item);
            existingConnections.add(connection);
            existingConnections.totalAmount += connection.amount;
        }

        Multimap<UUID, BigItemStack> toRequest = HashMultimap.create();

        for (Map.Entry<UUID, Map<ItemStack, ItemStackConnections>> entry : consolidated.entrySet()) {
            UUID network = entry.getKey();
            InventorySummary summary = LogisticsManager.getSummaryOfNetwork(network, true);

            for (ItemStackConnections connections : entry.getValue().values()) {
                if (connections.totalAmount == 0 || connections.item.isEmpty() || summary.getCountOf(connections.item) < connections.totalAmount) {
                    for (FactoryPanelConnection connection : connections)
                        sendEffect(connection.from, false);
                    failed = true;
                    continue;
                }

                BigItemStack stack = new BigItemStack(connections.item, connections.totalAmount);
                toRequest.put(network, stack);
                for (FactoryPanelConnection connection : connections)
                    sendEffect(connection.from, true);
            }
        }

        if (failed)
            return;

        // Input items may come from differing networks
        Map<UUID, Collection<BigItemStack>> asMap = toRequest.asMap();
        PackageOrderWithCrafts craftingContext = PackageOrderWithCrafts.empty();
        List<Multimap<PackagerBlockEntity, PackagingRequest>> requests = new ArrayList<>();

        // Panel may enforce item arrangement
        if (!activeCraftingArrangement.isEmpty())
            craftingContext = PackageOrderWithCrafts.singleRecipe(activeCraftingArrangement.stream()
                .map(stack -> new BigItemStack(stack.copyWithCount(1))).toList());

        // Collect request distributions
        for (Map.Entry<UUID, Collection<BigItemStack>> entry : asMap.entrySet()) {
            PackageOrderWithCrafts order = new PackageOrderWithCrafts(
                new PackageOrder(new ArrayList<>(entry.getValue())),
                craftingContext.orderedCrafts()
            );
            Multimap<PackagerBlockEntity, PackagingRequest> request = LogisticsManager.findPackagersForRequest(
                entry.getKey(),
                order,
                null,
                recipeAddress
            );
            requests.add(request);
        }

        // Check if any packager is busy - cancel all
        for (Multimap<PackagerBlockEntity, PackagingRequest> entry : requests)
            for (PackagerBlockEntity packager : entry.keySet())
                if (packager.isTooBusyFor(RequestType.RESTOCK))
                    return;

        // Send it
        for (Multimap<PackagerBlockEntity, PackagingRequest> entry : requests)
            LogisticsManager.performPackageRequests(entry);

        // Keep the output promise
        RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(network);
        if (promises != null)
            promises.add(new RequestPromise(new BigItemStack(getFilter(), recipeOutput)));

        panelBE.award(AllAdvancements.FACTORY_GAUGE);
    }

    private void tryRestock() {
        ItemStack item = getFilter();
        if (item.isEmpty())
            return;

        FactoryPanelBlockEntity panelBE = panelBE();
        PackagerBlockEntity packager = panelBE.getRestockedPackager();
        if (packager == null || !packager.targetInventory.hasInventory())
            return;

        int availableOnNetwork = LogisticsManager.getStockOf(network, item, packager.targetInventory.getIdentifiedInventory());
        if (availableOnNetwork == 0) {
            sendEffect(getPanelPosition(), false);
            return;
        }

        int inStorage = getLevelInStorage();
        int promised = getPromised();
        int maxStackSize = item.getMaxStackSize();
        int demand = getAmount() * (upTo ? 1 : maxStackSize);
        int amountToOrder = Math.clamp(demand - promised - inStorage, 0, maxStackSize * 9);

        BigItemStack orderedItem = new BigItemStack(item, Math.min(amountToOrder, availableOnNetwork));
        PackageOrderWithCrafts order = PackageOrderWithCrafts.simple(List.of(orderedItem));

        sendEffect(getPanelPosition(), true);

        if (!LogisticsManager.broadcastPackageRequest(
            network,
            RequestType.RESTOCK,
            order,
            packager.targetInventory.getIdentifiedInventory(),
            recipeAddress
        ))
            return;

        restockerPromises.add(new RequestPromise(orderedItem));
    }

    private void sendEffect(FactoryPanelPosition fromPos, boolean success) {
        if (getLevel() instanceof ServerLevel serverLevel) {
            BlockPos pos = getPos();
            serverLevel.getServer().getPlayerList().broadcast(
                null,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                64,
                serverLevel.dimension(),
                new FactoryPanelEffectPacket(fromPos, getPanelPosition(), success)
            );
        }
    }

    public void addConnection(FactoryPanelPosition fromPos) {
        FactoryPanelSupportBehaviour link = linkAt(getLevel(), fromPos);
        if (link != null) {
            targetedByLinks.put(fromPos.pos(), new FactoryPanelConnection(fromPos, 1));
            link.connect(this);
            blockEntity.notifyUpdate();
            return;
        }

        if (panelBE().restocker)
            return;
        if (targetedBy.size() >= 9)
            return;

        ServerFactoryPanelBehaviour source = at(getLevel(), fromPos);
        if (source == null)
            return;

        source.targeting.add(getPanelPosition());
        targetedBy.put(fromPos, new FactoryPanelConnection(fromPos, 1));
        searchForCraftingRecipe();
        blockEntity.notifyUpdate();
    }

    public FactoryPanelPosition getPanelPosition() {
        return new FactoryPanelPosition(getPos(), slot);
    }

    public FactoryPanelBlockEntity panelBE() {
        return (FactoryPanelBlockEntity) blockEntity;
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        // Network is protected
        if (!Create.LOGISTICS.mayInteract(network, player)) {
            player.displayClientMessage(Component.translatable("create.logistically_linked.protected").withStyle(ChatFormatting.RED), true);
            return;
        }

        boolean isClientSide = player.level().isClientSide();

        // Wrench cycles through arrow bending
        ItemStack heldItem = player.getItemInHand(hand);
        if (targeting.size() + targetedByLinks.size() > 0 && heldItem.is(AllItemTags.TOOLS_WRENCH)) {
            int sharedMode = -1;
            boolean notifySelf = false;

            for (FactoryPanelPosition target : targeting) {
                ServerFactoryPanelBehaviour at = at(getLevel(), target);
                if (at == null)
                    continue;
                FactoryPanelConnection connection = at.targetedBy.get(getPanelPosition());
                if (connection == null)
                    continue;
                if (sharedMode == -1)
                    sharedMode = (connection.arrowBendMode + 1) % 4;
                connection.arrowBendMode = sharedMode;
                if (!isClientSide)
                    at.blockEntity.notifyUpdate();
            }

            for (FactoryPanelConnection connection : targetedByLinks.values()) {
                if (sharedMode == -1)
                    sharedMode = (connection.arrowBendMode + 1) % 4;
                connection.arrowBendMode = sharedMode;
                if (!isClientSide)
                    notifySelf = true;
            }

            if (sharedMode == -1)
                return;

            char[] boxes = "□□□□".toCharArray();
            boxes[sharedMode] = '■';
            player.displayClientMessage(Component.translatable("create.factory_panel.cycled_arrow_path", new String(boxes)), true);
            if (notifySelf)
                blockEntity.notifyUpdate();

            return;
        }

        // Client might be in the process of connecting a panel
        if (isClientSide)
            if (AllClientHandle.INSTANCE.factoryPanelClicked(getLevel(), player, this))
                return;

        if (getFilter().isEmpty()) {
            // Open screen for setting an item through JEI
            if (heldItem.isEmpty()) {
                if (!isClientSide && player instanceof ServerPlayer sp)
                    openHandledScreen(sp);
                return;
            }

            // Use regular filter interaction for setting the item
            super.onShortInteract(player, hand, side, hitResult);
            return;
        }

        // Bind logistics items to this panels' frequency
        if (heldItem.getItem() instanceof LogisticallyLinkedBlockItem) {
            if (!isClientSide)
                LogisticallyLinkedBlockItem.assignFrequency(heldItem, player, network);
            return;
        }

        // Open configuration screen
        if (isClientSide)
            AllClientHandle.INSTANCE.openFactoryPanelScreen(this, player);
    }

    public void enable() {
        active = true;
        blockEntity.notifyUpdate();
    }

    public void disable() {
        destroy();
        active = false;
        targetedBy = new HashMap<>();
        targeting = new HashSet<>();
        count = 0;
        satisfied = false;
        promisedSatisfied = false;
        recipeAddress = "";
        recipeOutput = 1;
        setFilter(ItemStack.EMPTY);
        blockEntity.notifyUpdate();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public boolean isMissingAddress() {
        return (!targetedBy.isEmpty() || panelBE().restocker) && count != 0 && recipeAddress.isBlank();
    }

    @Override
    public void destroy() {
        disconnectAll();
        super.destroy();
    }

    public void disconnectAll() {
        FactoryPanelPosition panelPosition = getPanelPosition();
        disconnectAllLinks();
        for (FactoryPanelConnection connection : targetedBy.values()) {
            ServerFactoryPanelBehaviour source = at(getLevel(), connection);
            if (source != null) {
                source.targeting.remove(panelPosition);
                source.blockEntity.sendData();
            }
        }
        for (FactoryPanelPosition position : targeting) {
            ServerFactoryPanelBehaviour target = at(getLevel(), position);
            if (target != null) {
                target.targetedBy.remove(panelPosition);
                target.searchForCraftingRecipe();
                target.blockEntity.sendData();
            }
        }
        targetedBy.clear();
        targeting.clear();
    }

    public void disconnectAllLinks() {
        for (FactoryPanelConnection connection : targetedByLinks.values()) {
            FactoryPanelSupportBehaviour source = linkAt(getLevel(), connection);
            if (source != null)
                source.disconnect(this);
        }
        targetedByLinks.clear();
    }

    public int getUnloadedLinks() {
        if (getLevel().isClientSide())
            return lastReportedUnloadedLinks;
        if (panelBE().restocker)
            return panelBE().getRestockedPackager() == null ? 1 : 0;
        return Create.LOGISTICS.getUnloadedLinkCount(network);
    }

    public int getLevelInStorage() {
        if (blockEntity.isVirtual())
            return 1;
        if (getLevel().isClientSide())
            return lastReportedLevelInStorage;
        if (getFilter().isEmpty())
            return 0;

        InventorySummary summary = getRelevantSummary();
        return summary.getCountOf(getFilter());
    }

    private InventorySummary getRelevantSummary() {
        FactoryPanelBlockEntity panelBE = panelBE();
        if (!panelBE.restocker)
            return LogisticsManager.getSummaryOfNetwork(network, false);
        PackagerBlockEntity packager = panelBE.getRestockedPackager();
        if (packager == null)
            return InventorySummary.EMPTY;
        return packager.getAvailableItems();
    }

    public int getPromised() {
        if (getLevel().isClientSide())
            return lastReportedPromises;
        ItemStack item = getFilter();
        if (item.isEmpty())
            return 0;

        if (panelBE().restocker) {
            if (forceClearPromises) {
                restockerPromises.forceClear(item);
                resetTimerSlightly();
            }
            forceClearPromises = false;
            return restockerPromises.getTotalPromisedAndRemoveExpired(item, getPromiseExpiryTimeInTicks());
        }

        RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(network);
        if (promises == null)
            return 0;

        if (forceClearPromises) {
            promises.forceClear(item);
            resetTimerSlightly();
        }
        forceClearPromises = false;

        return promises.getTotalPromisedAndRemoveExpired(item, getPromiseExpiryTimeInTicks());
    }

    public void resetTimer() {
        timer = getConfigRequestIntervalInTicks();
    }

    public void resetTimerSlightly() {
        timer = getConfigRequestIntervalInTicks() / 2;
    }

    private int getConfigRequestIntervalInTicks() {
        return AllConfigs.server().logistics.factoryGaugeTimer.get();
    }

    private int getPromiseExpiryTimeInTicks() {
        if (promiseClearingInterval == -1)
            return -1;
        if (promiseClearingInterval == 0)
            return 20 * 30;

        return promiseClearingInterval * 20 * 60;
    }

    @Override
    public void writeSafe(ValueOutput view) {
        if (!active)
            return;

        ValueOutput panelTag = view.child(slot.name().toLowerCase(Locale.ROOT));
        panelTag.store("Filter", FilterItemStack.CODEC, filter);
        panelTag.putBoolean("UpTo", upTo);
        panelTag.putInt("FilterAmount", count);
        panelTag.store("Freq", UUIDUtil.CODEC, network);
        panelTag.putString("RecipeAddress", recipeAddress);
        panelTag.putInt("PromiseClearingInterval", -1);
        panelTag.putInt("RecipeOutput", 1);

        if (panelBE().restocker)
            panelTag.store("Promises", RequestPromiseQueue.CODEC, restockerPromises);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        if (!active)
            return;

        ValueOutput panelTag = view.child(slot.name().toLowerCase(Locale.ROOT));
        super.write(panelTag, clientPacket);

        panelTag.putInt("Timer", timer);
        panelTag.putInt("LastLevel", lastReportedLevelInStorage);
        panelTag.putInt("LastPromised", lastReportedPromises);
        panelTag.putInt("LastUnloadedLinks", lastReportedUnloadedLinks);
        panelTag.putBoolean("Satisfied", satisfied);
        panelTag.putBoolean("PromisedSatisfied", promisedSatisfied);
        panelTag.putBoolean("Waiting", waitingForNetwork);
        panelTag.putBoolean("RedstonePowered", redstonePowered);
        panelTag.store("Targeting", TARGET_CODEC, targeting);
        TypedOutputList<FactoryPanelConnection> targetedByList = panelTag.list("TargetedBy", FactoryPanelConnection.CODEC);
        targetedBy.values().forEach(targetedByList::add);
        TypedOutputList<FactoryPanelConnection> targetedByLinkList = panelTag.list("TargetedByLinks", FactoryPanelConnection.CODEC);
        targetedByLinks.values().forEach(targetedByLinkList::add);
        panelTag.putString("RecipeAddress", recipeAddress);
        panelTag.putInt("RecipeOutput", recipeOutput);
        panelTag.putInt("PromiseClearingInterval", promiseClearingInterval);
        panelTag.store("Freq", UUIDUtil.CODEC, network);
        panelTag.store("Craft", CreateCodecs.ITEM_LIST_CODEC, activeCraftingArrangement);
        if (craftingList != null) {
            panelTag.store("CraftingList", CRAFTING_LIST_CODEC, craftingList);
        }

        if (panelBE().restocker && !clientPacket)
            panelTag.store("Promises", RequestPromiseQueue.CODEC, restockerPromises);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        Optional<ValueInput> slotView = view.child(slot.name().toLowerCase(Locale.ROOT));
        if (slotView.isEmpty()) {
            active = false;
            return;
        }
        ValueInput panelTag = slotView.get();

        active = true;
        filter = panelTag.read("Filter", FilterItemStack.CODEC).orElseGet(FilterItemStack::empty);
        count = panelTag.getIntOr("FilterAmount", 0);
        upTo = panelTag.getBooleanOr("UpTo", false);
        timer = panelTag.getIntOr("Timer", 0);
        lastReportedLevelInStorage = panelTag.getIntOr("LastLevel", 0);
        lastReportedPromises = panelTag.getIntOr("LastPromised", 0);
        lastReportedUnloadedLinks = panelTag.getIntOr("LastUnloadedLinks", 0);
        satisfied = panelTag.getBooleanOr("Satisfied", false);
        promisedSatisfied = panelTag.getBooleanOr("PromisedSatisfied", false);
        waitingForNetwork = panelTag.getBooleanOr("Waiting", false);
        redstonePowered = panelTag.getBooleanOr("RedstonePowered", false);
        promiseClearingInterval = panelTag.getIntOr("PromiseClearingInterval", 0);
        panelTag.read("Freq", UUIDUtil.CODEC).ifPresent(uuid -> network = uuid);

        targeting.clear();
        panelTag.read("Targeting", TARGET_CODEC).ifPresent(targeting::addAll);

        targetedBy.clear();
        panelTag.listOrEmpty("TargetedBy", FactoryPanelConnection.CODEC).forEach(c -> targetedBy.put(c.from, c));

        targetedByLinks.clear();
        panelTag.listOrEmpty("TargetedByLinks", FactoryPanelConnection.CODEC).forEach(c -> targetedByLinks.put(c.from.pos(), c));

        activeCraftingArrangement = panelTag.read("Craft", CreateCodecs.ITEM_LIST_CODEC).orElseGet(List::of);
        recipeAddress = panelTag.getStringOr("RecipeAddress", "");
        recipeOutput = panelTag.getIntOr("RecipeOutput", 0);

        if (view.getBooleanOr("Restocker", false) && !clientPacket) {
            Optional<RequestPromiseQueue> queue = panelTag.read("Promises", RequestPromiseQueue.CODEC);
            if (queue.isPresent()) {
                restockerPromises = queue.get();
                restockerPromises.setOnChanged(() -> {
                });
            } else {
                restockerPromises = new RequestPromiseQueue(() -> {
                });
            }
            promisePrimedForMarkDirty = false;
        }

        craftingList = panelTag.read("CraftingList", CRAFTING_LIST_CODEC).orElse(null);
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        ItemStack filter = stack.copy();
        if (stack.getItem() instanceof FilterItem)
            return false;
        this.filter = FilterItemStack.of(filter);
        searchForCraftingRecipe();
        blockEntity.setChanged();
        blockEntity.sendData();
        return true;
    }

    public void searchForCraftingRecipe() {
        if (!(getLevel() instanceof ServerLevel serverWorld)) {
            return;
        }
        ItemStack output = filter.item();
        if (output.isEmpty() || targetedBy.isEmpty()) {
            craftingList = null;
            return;
        }
        List<BigItemStack> inputConfig = targetedBy.values().stream().map(c -> {
            ServerFactoryPanelBehaviour b = ServerFactoryPanelBehaviour.at(serverWorld, c.from);
            return b == null ? new BigItemStack(ItemStack.EMPTY, 0) : new BigItemStack(b.getFilter(), c.amount);
        }).toList();

        Set<Item> itemsToUse = inputConfig.stream().map(b -> b.stack).filter(i -> !i.isEmpty()).map(ItemStack::getItem).collect(Collectors.toSet());

        Item item = output.getItem();
        RegistryAccess registryManager = serverWorld.registryAccess();
        CraftingRecipe availableCraftingRecipe = serverWorld.recipeAccess().recipes.byType(RecipeType.CRAFTING).parallelStream().filter(entry -> {
            if (AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
                return false;
            }
            CraftingRecipe recipe = entry.value();
            List<Ingredient> ingredients;
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                ingredients = shapedRecipe.getIngredients().stream().flatMap(Optional::stream).toList();
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                ingredients = shapelessRecipe.ingredients;
            } else {
                return false;
            }
            try {
                ItemStack result = recipe.assemble(CraftingInput.EMPTY);
                if (result.getItem() != item) {
                    return false;
                }
            } catch (Exception ignore) {
                return false;
            }
            Set<Item> itemsUsed = new HashSet<>();
            for (Ingredient ingredient : ingredients) {
                if (ingredient.isEmpty())
                    continue;
                boolean available = false;
                for (BigItemStack bis : inputConfig) {
                    if (!bis.stack.isEmpty() && ingredient.test(bis.stack)) {
                        available = true;
                        itemsUsed.add(bis.stack.getItem());
                        break;
                    }
                }
                if (!available)
                    return false;
            }

            return itemsUsed.size() >= itemsToUse.size();
        }).findAny().map(RecipeHolder::value).orElse(null);
        if (availableCraftingRecipe == null) {
            craftingList = null;
            return;
        }
        craftingList = convertRecipeToPackageOrderContext(availableCraftingRecipe, registryManager, inputConfig, false);
    }

    @Nullable
    public static List<BigItemStack> convertRecipeToPackageOrderContext(
        CraftingRecipe availableCraftingRecipe,
        RegistryAccess registryManager,
        List<BigItemStack> inputs,
        boolean respectAmounts
    ) {
        List<@Nullable Ingredient> ingredients;
        if (availableCraftingRecipe instanceof ShapedRecipe shapedRecipe) {
            ingredients = shapedRecipe.getIngredients().stream().map(o -> o.orElse(null)).toList();
        } else if (availableCraftingRecipe instanceof ShapelessRecipe shapelessRecipe) {
            ingredients = shapelessRecipe.ingredients;
        } else {
            return null;
        }

        List<BigItemStack> craftingList = new ArrayList<>();
        ItemStack output = availableCraftingRecipe.assemble(CraftingInput.EMPTY);
        int count = output.getCount();
        output.setCount(1);
        craftingList.add(new BigItemStack(output, count));

        BigItemStack emptyIngredient = new BigItemStack(ItemStack.EMPTY, 1);
        List<BigItemStack> mutableInputs = BigItemStack.duplicateWrappers(inputs);

        int width = Math.min(3, ingredients.size());
        int height = Math.min(3, ingredients.size() / 3 + 1);

        if (availableCraftingRecipe instanceof ShapedRecipe shaped) {
            width = shaped.getWidth();
            height = shaped.getHeight();
        }

        if (height == 1)
            for (int i = 0; i < 3; i++)
                craftingList.add(emptyIngredient);
        if (width == 1)
            craftingList.add(emptyIngredient);

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            BigItemStack craftingIngredient = emptyIngredient;

            if (ingredient != null && !ingredient.isEmpty())
                for (BigItemStack bigItemStack : mutableInputs)
                    if (bigItemStack.count > 0 && ingredient.test(bigItemStack.stack)) {
                        craftingIngredient = new BigItemStack(bigItemStack.stack, 1);
                        if (respectAmounts)
                            bigItemStack.count -= 1;
                        break;
                    }

            craftingList.add(craftingIngredient);

            if (width < 3 && (i + 1) % width == 0)
                for (int j = 0; j < 3 - width; j++)
                    if (craftingList.size() < 10)
                        craftingList.add(emptyIngredient);
        }

        while (craftingList.size() < 10)
            craftingList.add(emptyIngredient);

        return craftingList;
    }

    @Override
    public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
        if (getValueSettings().equals(settings))
            return;
        count = Math.max(0, settings.value());
        upTo = settings.row() == 0;
        panelBE().redraw = true;
        blockEntity.setChanged();
        blockEntity.sendData();
        playFeedbackSound(this);
        resetTimerSlightly();
        if (!getLevel().isClientSide())
            notifyRedstoneOutputs();
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(upTo ? 0 : 1, count);
    }

    @Override
    public int netId() {
        return 2 + slot.ordinal();
    }

    @Override
    public boolean isCountVisible() {
        return !getFilter().isEmpty();
    }

    @Override
    public BehaviourType<?> getType() {
        return getTypeForSlot(slot);
    }

    public static BehaviourType<ServerFactoryPanelBehaviour> getTypeForSlot(PanelSlot slot) {
        return switch (slot) {
            case BOTTOM_LEFT -> BOTTOM_LEFT;
            case TOP_LEFT -> TOP_LEFT;
            case TOP_RIGHT -> TOP_RIGHT;
            case BOTTOM_RIGHT -> BOTTOM_RIGHT;
        };
    }

    public int getIngredientStatusColor() {
        return count == 0 || isMissingAddress() || redstonePowered ? 0x888898 : waitingForNetwork ? 0x5B3B3B : satisfied ? 0x9EFF7F : promisedSatisfied ? 0x22AFAF : 0x3D6EBD;
    }

    @Override
    public ItemRequirement getRequiredItems() {
        return isActive() ? new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, AllBlocks.FACTORY_GAUGE.asItem()) : ItemRequirement.NONE;
    }

    @Override
    public boolean canShortInteract(ItemStack toApply) {
        return true;
    }

    @Override
    public boolean readFromClipboard(ValueInput view, Player player, Direction side, boolean simulate) {
        return false;
    }

    @Override
    public boolean canWrite(HolderLookup.Provider registries, Direction side) {
        return false;
    }

    @Override
    public boolean writeToClipboard(ValueOutput view, Direction side) {
        return false;
    }

    @Override
    public FactoryPanelSetItemMenu createMenu(int containerId, Inventory playerInventory, Player player, RegistryFriendlyByteBuf extraData) {
        FactoryPanelPosition.PACKET_CODEC.encode(extraData, getPanelPosition());
        return new FactoryPanelSetItemMenu(containerId, playerInventory, this);
    }

    @Override
    public Component getDisplayName() {
        return blockEntity.getBlockState().getBlock().getName();
    }

    @Nullable
    public String getFrogAddress() {
        PackagerBlockEntity packager = panelBE().getRestockedPackager();
        if (packager == null)
            return null;
        if (packager.getLevel().getBlockEntity(packager.getBlockPos().above()) instanceof FrogportBlockEntity fpbe)
            if (fpbe.addressFilter != null && !fpbe.addressFilter.isBlank())
                return fpbe.addressFilter;
        return null;
    }

}
