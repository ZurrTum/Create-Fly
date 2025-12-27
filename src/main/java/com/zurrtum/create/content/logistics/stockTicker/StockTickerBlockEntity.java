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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class StockTickerBlockEntity extends StockCheckingBlockEntity implements Clearable {
    public static final Codec<Map<UUID, List<Integer>>> UUID_MAP_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT.listOf());

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

    public void resetTicksSinceLastUpdate() {
        ticksSinceLastUpdate = 0;
    }

    public Container getReceivedPaymentsHandler() {
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
        if (level.isClientSide()) {
            if (ticksSinceLastUpdate < 100)
                ticksSinceLastUpdate += 1;
        }
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putString("PreviousAddress", previouslyUsedAddress);
        receivedPayments.write(view);
        view.store("Categories", CreateCodecs.ITEM_LIST_CODEC, categories);
        view.store("HiddenCategories", UUID_MAP_CODEC, hiddenCategoriesByPlayer);

        if (clientPacket)
            view.putInt("ActiveLinks", activeLinks);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        previouslyUsedAddress = view.getStringOr("PreviousAddress", "");
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
            activeLinks = view.getIntOr("ActiveLinks", 0);
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
                    if (!filterItemStack.test(level, bigStack.stack))
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
                BlockPos seatPos = worldPosition.below(yOffset).relative(side);
                for (SeatEntity seatEntity : level.getEntitiesOfClass(SeatEntity.class, new AABB(seatPos)))
                    if (seatEntity.isVehicle())
                        return true;
                if (yOffset == 0) {
                    BlockEntity entity = level.getBlockEntity(seatPos);
                    if (entity != null && entity.getType() == AllBlockEntityTypes.HEATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void clearContent() {
        categories.clear();
        receivedPayments.clearContent();
    }

    @Override
    public void destroy() {
        Containers.dropContents(level, worldPosition, receivedPayments);
        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();
        for (ItemStack filter : categories) {
            if (!filter.isEmpty() && filter.getItem() instanceof FilterItem) {
                Containers.dropItemStack(level, x, y, z, filter);
            }
        }
        super.destroy();
    }

    public void playEffect() {
        AllSoundEvents.STOCK_LINK.playAt(level, worldPosition, 1.0f, 1.0f, false);
        Vec3 vec3 = Vec3.atCenterOf(worldPosition);
        level.addParticle(AllParticleTypes.WIFI, vec3.x, vec3.y, vec3.z, 1, 1, 1);
    }

    public StockKeeperCategoryMenu createCategoryMenu(
        int pContainerId,
        Inventory pPlayerInventory,
        Player pPlayer,
        RegistryFriendlyByteBuf extraData
    ) {
        extraData.writeBlockPos(worldPosition);
        return new StockKeeperCategoryMenu(pContainerId, pPlayerInventory, StockTickerBlockEntity.this);
    }

    public StockKeeperRequestMenu createRequestMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer, RegistryFriendlyByteBuf extraData) {
        boolean showLockOption = behaviour.mayAdministrate(pPlayer) && Create.LOGISTICS.isLockable(behaviour.freqId);
        boolean isCurrentlyLocked = Create.LOGISTICS.isLocked(behaviour.freqId);
        extraData.writeBlockPos(worldPosition);
        extraData.writeBoolean(showLockOption);
        extraData.writeBoolean(isCurrentlyLocked);
        return new StockKeeperRequestMenu(pContainerId, pPlayerInventory, this);
    }

    public class StockTickerInventory extends ItemStackHandler {
        public StockTickerInventory() {
            super(27);
        }

        @Override
        public void setChanged() {
            notifyUpdate();
        }
    }
}