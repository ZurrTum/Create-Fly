package com.zurrtum.create.content.logistics.stockTicker;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.logistics.packager.IdentifiedInventory;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class StockTickerBlockEntity extends StockCheckingBlockEntity {
    public static final Codec<Map<UUID, List<Integer>>> UUID_MAP_CODEC = Codec.unboundedMap(Uuids.STRING_CODEC, Codec.INT.listOf());

    //TODO
    //    public AbstractComputerBehaviour computerBehaviour;

    // Player-interface Feature
    public List<List<BigItemStack>> lastClientsideStockSnapshot;
    protected InventorySummary lastClientsideStockSnapshotAsSummary;
    protected List<BigItemStack> newlyReceivedStockSnapshot;
    public String previouslyUsedAddress;
    public int activeLinks;
    public int ticksSinceLastUpdate;
    public List<ItemStack> categories;
    public Map<UUID, List<Integer>> hiddenCategoriesByPlayer;

    // Shop feature
    public StockTickerInventory receivedPayments;

    public StockTickerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.STOCK_TICKER, pos, state);
        previouslyUsedAddress = "";
        receivedPayments = new StockTickerInventory();
        categories = new ArrayList<>();
        hiddenCategoriesByPlayer = new HashMap<>();
    }

    //TODO
    //    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    //        if (Mods.COMPUTERCRAFT.isLoaded()) {
    //            event.registerBlockEntity(
    //                PeripheralCapability.get(),
    //                AllBlockEntityTypes.STOCK_TICKER.get(),
    //                (be, context) -> be.computerBehaviour.getPeripheralCapability()
    //            );
    //        }
    //    }

    //TODO
    //    @Override
    //    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    //        super.addBehaviours(behaviours);
    //        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    //    }

    //TODO
    //    @Override
    //    public void invalidate() {
    //        super.invalidate();
    //        computerBehaviour.removePeripheral();
    //    }

    public void resetTicksSinceLastUpdate() {
        ticksSinceLastUpdate = 0;
    }

    public Inventory getReceivedPaymentsHandler() {
        return receivedPayments;
    }

    public List<List<BigItemStack>> getClientStockSnapshot() {
        return lastClientsideStockSnapshot;
    }

    public InventorySummary getLastClientsideStockSnapshotAsSummary() {
        return lastClientsideStockSnapshotAsSummary;
    }

    public int getTicksSinceLastUpdate() {
        return ticksSinceLastUpdate;
    }

    @Override
    public boolean broadcastPackageRequest(RequestType type, PackageOrderWithCrafts order, IdentifiedInventory ignoredHandler, String address) {
        boolean result = super.broadcastPackageRequest(type, order, ignoredHandler, address);
        previouslyUsedAddress = address;
        notifyUpdate();
        return result;
    }

    @Override
    public InventorySummary getRecentSummary() {
        InventorySummary recentSummary = super.getRecentSummary();
        int contributingLinks = recentSummary.contributingLinks;
        if (activeLinks != contributingLinks && !isRemoved()) {
            activeLinks = contributingLinks;
            sendData();
        }
        return recentSummary;
    }

    @Override
    public void tick() {
        super.tick();
        if (world.isClient) {
            if (ticksSinceLastUpdate < 100)
                ticksSinceLastUpdate += 1;
        }
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putString("PreviousAddress", previouslyUsedAddress);
        receivedPayments.write(view);
        view.put("Categories", CreateCodecs.ITEM_LIST_CODEC, categories);
        view.put("HiddenCategories", UUID_MAP_CODEC, hiddenCategoriesByPlayer);

        if (clientPacket)
            view.putInt("ActiveLinks", activeLinks);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        previouslyUsedAddress = view.getString("PreviousAddress", "");
        receivedPayments.read(view);
        categories.clear();
        view.read("Categories", CreateCodecs.ITEM_LIST_CODEC).ifPresent(list -> list.forEach(stack -> {
            if (!stack.isEmpty() && !(stack.getItem() instanceof FilterItem)) {
                return;
            }
            categories.add(stack);
        }));
        hiddenCategoriesByPlayer.clear();
        view.read("HiddenCategories", UUID_MAP_CODEC).ifPresent(map -> hiddenCategoriesByPlayer.putAll(map));

        if (clientPacket)
            activeLinks = view.getInt("ActiveLinks", 0);
    }

    public void receiveStockPacket(List<BigItemStack> stacks, boolean endOfTransmission) {
        if (newlyReceivedStockSnapshot == null)
            newlyReceivedStockSnapshot = new ArrayList<>();
        newlyReceivedStockSnapshot.addAll(stacks);

        if (!endOfTransmission)
            return;

        lastClientsideStockSnapshotAsSummary = new InventorySummary();
        lastClientsideStockSnapshot = new ArrayList<>();

        for (BigItemStack bigStack : newlyReceivedStockSnapshot)
            lastClientsideStockSnapshotAsSummary.add(bigStack);

        for (ItemStack filter : categories) {
            List<BigItemStack> inCategory = new ArrayList<>();
            if (!filter.isEmpty()) {
                FilterItemStack filterItemStack = FilterItemStack.of(filter);
                for (Iterator<BigItemStack> iterator = newlyReceivedStockSnapshot.iterator(); iterator.hasNext(); ) {
                    BigItemStack bigStack = iterator.next();
                    if (!filterItemStack.test(world, bigStack.stack))
                        continue;
                    inCategory.add(bigStack);
                    iterator.remove();
                }
            }
            lastClientsideStockSnapshot.add(inCategory);
        }

        List<BigItemStack> unsorted = new ArrayList<>(newlyReceivedStockSnapshot);
        lastClientsideStockSnapshot.add(unsorted);
        newlyReceivedStockSnapshot = null;
    }

    public boolean isKeeperPresent() {
        for (int yOffset : Iterate.zeroAndOne) {
            for (Direction side : Iterate.horizontalDirections) {
                BlockPos seatPos = pos.down(yOffset).offset(side);
                int x = seatPos.getX();
                int y = seatPos.getY();
                int z = seatPos.getZ();
                for (SeatEntity seatEntity : world.getNonSpectatingEntities(SeatEntity.class, new Box(x, y - 0.1f, z, x + 1, y + 1, z + 1)))
                    if (seatEntity.hasPassengers())
                        return true;
                if (yOffset == 0) {
                    BlockEntity entity = world.getBlockEntity(seatPos);
                    if (entity != null && entity.getType() == AllBlockEntityTypes.HEATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        ItemScatterer.spawn(world, pos, receivedPayments);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (ItemStack filter : categories) {
            if (!filter.isEmpty() && filter.getItem() instanceof FilterItem) {
                ItemScatterer.spawn(world, x, y, z, filter);
            }
        }
        super.destroy();
    }

    public void playEffect() {
        AllSoundEvents.STOCK_LINK.playAt(world, pos, 1.0f, 1.0f, false);
        Vec3d vec3 = Vec3d.ofCenter(pos);
        world.addParticleClient(AllParticleTypes.WIFI, vec3.x, vec3.y, vec3.z, 1, 1, 1);
    }

    public StockKeeperCategoryMenu createCategoryMenu(
        int pContainerId,
        PlayerInventory pPlayerInventory,
        PlayerEntity pPlayer,
        RegistryByteBuf extraData
    ) {
        extraData.writeBlockPos(pos);
        return new StockKeeperCategoryMenu(pContainerId, pPlayerInventory, StockTickerBlockEntity.this);
    }

    public StockKeeperRequestMenu createRequestMenu(
        int pContainerId,
        PlayerInventory pPlayerInventory,
        PlayerEntity pPlayer,
        RegistryByteBuf extraData
    ) {
        boolean showLockOption = behaviour.mayAdministrate(pPlayer) && Create.LOGISTICS.isLockable(behaviour.freqId);
        boolean isCurrentlyLocked = Create.LOGISTICS.isLocked(behaviour.freqId);
        extraData.writeBlockPos(pos);
        extraData.writeBoolean(showLockOption);
        extraData.writeBoolean(isCurrentlyLocked);
        return new StockKeeperRequestMenu(pContainerId, pPlayerInventory, this);
    }

    public class StockTickerInventory extends ItemStackHandler {
        public StockTickerInventory() {
            super(27);
        }

        @Override
        public void markDirty() {
            notifyUpdate();
        }
    }
}