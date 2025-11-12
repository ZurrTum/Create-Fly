package com.zurrtum.create.content.contraptions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.zurrtum.create.AllMountedItemStorageTypeTags;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.contraption.storage.SyncedMountedStorage;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.items.CombinedInvWrapper;
import com.zurrtum.create.infrastructure.packet.s2c.MountedStorageSyncPacket;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MountedStorageManager {
    // builders used during assembly, null afterward
    // ImmutableMap.Builder is not used because it will throw with duplicate keys, not override them
    private Map<BlockPos, MountedItemStorage> itemsBuilder;
    private Map<BlockPos, MountedFluidStorage> fluidsBuilder;
    private Map<BlockPos, SyncedMountedStorage> syncedItemsBuilder;
    private Map<BlockPos, SyncedMountedStorage> syncedFluidsBuilder;

    // built data structures after assembly, null before
    private ImmutableMap<BlockPos, MountedItemStorage> allItemStorages;
    // different from allItemStorages, does not contain internal ones
    protected MountedItemStorageWrapper items;
    @Nullable
    protected MountedItemStorageWrapper fuelItems;
    protected MountedFluidStorageWrapper fluids;

    private ImmutableMap<BlockPos, SyncedMountedStorage> syncedItems;
    private ImmutableMap<BlockPos, SyncedMountedStorage> syncedFluids;

    private List<Container> externalHandlers;
    protected CombinedInvWrapper allItems;

    // ticks until storage can sync again
    private int syncCooldown;

    // client-side: not all storages are synced, this determines which interactions are valid
    private Set<BlockPos> interactablePositions;

    public MountedStorageManager() {
        this.reset();
    }

    public void initialize() {
        if (this.isInitialized()) {
            // originally this threw an exception to try to catch mistakes.
            // however, in the case where a Contraption is deserialized before its Entity, that would also throw,
            // since both the deserialization and the onEntityCreated callback initialize the storage.
            // this case occurs when placing a picked up minecart contraption.
            // the reverse case is fine since deserialization also resets the manager first.
            return;
        }

        this.allItemStorages = ImmutableMap.copyOf(this.itemsBuilder);

        this.items = new MountedItemStorageWrapper(subMap(this.allItemStorages, this::isExposed));

        this.allItems = this.items;
        this.itemsBuilder = null;

        ImmutableMap<BlockPos, MountedItemStorage> fuelMap = subMap(this.allItemStorages, this::canUseForFuel);
        this.fuelItems = fuelMap.isEmpty() ? null : new MountedItemStorageWrapper(fuelMap);

        ImmutableMap<BlockPos, MountedFluidStorage> fluids = ImmutableMap.copyOf(this.fluidsBuilder);
        this.fluids = new MountedFluidStorageWrapper(fluids);
        this.fluidsBuilder = null;

        this.syncedItems = ImmutableMap.copyOf(this.syncedItemsBuilder);
        this.syncedItemsBuilder = null;
        this.syncedFluids = ImmutableMap.copyOf(this.syncedFluidsBuilder);
        this.syncedFluidsBuilder = null;
    }

    private boolean isExposed(MountedItemStorage storage) {
        return !storage.type.is(AllMountedItemStorageTypeTags.INTERNAL);
    }

    private boolean canUseForFuel(MountedItemStorage storage) {
        return this.isExposed(storage) && !storage.type.is(AllMountedItemStorageTypeTags.FUEL_BLACKLIST);
    }

    private boolean isInitialized() {
        return this.itemsBuilder == null;
    }

    private void assertInitialized() {
        if (!this.isInitialized()) {
            throw new IllegalStateException("MountedStorageManager is uninitialized");
        }
    }

    protected void reset() {
        this.allItemStorages = null;
        this.items = null;
        this.fuelItems = null;
        this.fluids = null;
        this.externalHandlers = new ArrayList<>();
        this.allItems = null;
        this.itemsBuilder = new HashMap<>();
        this.fluidsBuilder = new HashMap<>();
        this.syncedItemsBuilder = new HashMap<>();
        this.syncedFluidsBuilder = new HashMap<>();
        // interactablePositions intentionally not reset
    }

    public void addBlock(Level level, BlockState state, BlockPos globalPos, BlockPos localPos, @Nullable BlockEntity be) {
        MountedItemStorageType<?> itemType = MountedItemStorageType.REGISTRY.get(state.getBlock());
        if (itemType != null) {
            MountedItemStorage storage = itemType.mount(level, state, globalPos, be);
            if (storage != null) {
                this.addStorage(storage, localPos);
            }
        }

        MountedFluidStorageType<?> fluidType = MountedFluidStorageType.REGISTRY.get(state.getBlock());
        if (fluidType != null) {
            MountedFluidStorage storage = fluidType.mount(level, state, globalPos, be);
            if (storage != null) {
                this.addStorage(storage, localPos);
            }
        }
    }

    public void unmount(Level level, StructureBlockInfo info, BlockPos globalPos, @Nullable BlockEntity be) {
        BlockPos localPos = info.pos();
        BlockState state = info.state();

        MountedItemStorage itemStorage = this.getAllItemStorages().get(localPos);
        if (itemStorage != null) {
            MountedItemStorageType<?> expectedType = MountedItemStorageType.REGISTRY.get(state.getBlock());
            if (itemStorage.type == expectedType) {
                itemStorage.unmount(level, state, globalPos, be);
            }
        }

        MountedFluidStorage fluidStorage = this.getFluids().storages.get(localPos);
        if (fluidStorage != null) {
            MountedFluidStorageType<?> expectedType = MountedFluidStorageType.REGISTRY.get(state.getBlock());
            if (fluidStorage.type == expectedType) {
                fluidStorage.unmount(level, state, globalPos, be);
            }
        }
    }

    public void tick(AbstractContraptionEntity entity) {
        if (syncCooldown > 0) {
            syncCooldown--;
            return;
        }

        Map<BlockPos, MountedItemStorage> items = new HashMap<>();
        Map<BlockPos, MountedFluidStorage> fluids = new HashMap<>();
        syncedItems.forEach((pos, storage) -> {
            if (storage.isDirty()) {
                items.put(pos, (MountedItemStorage) storage);
                storage.markClean();
            }
        });
        syncedFluids.forEach((pos, storage) -> {
            if (storage.isDirty()) {
                fluids.put(pos, (MountedFluidStorage) storage);
                storage.markClean();
            }
        });

        if (!items.isEmpty() || !fluids.isEmpty()) {
            Packet<ClientGamePacketListener> packet = new MountedStorageSyncPacket(entity.getId(), items, fluids);
            ((ServerChunkCache) entity.level().getChunkSource()).sendToTrackingPlayers(entity, packet);
            syncCooldown = 8;
        }
    }

    public void handleSync(MountedStorageSyncPacket packet, AbstractContraptionEntity entity) {
        // packet only contains changed storages, grab existing ones before resetting
        ImmutableMap<BlockPos, MountedItemStorage> items = this.getAllItemStorages();
        MountedFluidStorageWrapper fluids = this.getFluids();
        this.reset();

        // track freshly synced storages
        Map<SyncedMountedStorage, BlockPos> syncedStorages = new IdentityHashMap<>();

        try {
            // re-add existing ones
            this.itemsBuilder.putAll(items);
            this.fluidsBuilder.putAll(fluids.storages);
            // add newly synced ones, overriding existing ones if present
            packet.items().forEach((pos, storage) -> {
                this.itemsBuilder.put(pos, storage);
                syncedStorages.put((SyncedMountedStorage) storage, pos);
            });
            packet.fluids().forEach((pos, storage) -> {
                this.fluidsBuilder.put(pos, storage);
                syncedStorages.put((SyncedMountedStorage) storage, pos);
            });
        } catch (Throwable t) {
            // an exception will leave the manager in an invalid state
            Create.LOGGER.error("An error occurred while syncing a MountedStorageManager", t);
        }

        this.initialize();

        // call all afterSync methods
        Contraption contraption = entity.getContraption();
        syncedStorages.forEach((storage, pos) -> storage.afterSync(contraption, pos));
    }

    // contraption is provided on the client for initial afterSync storage callbacks
    public void read(ValueInput view, boolean clientPacket, @Nullable Contraption contraption) {
        reset();

        try {
            view.childrenListOrEmpty("items").forEach(item -> {
                BlockPos pos = item.read("pos", BlockPos.CODEC).orElseThrow();
                MountedItemStorage storage = item.read("storage", MountedItemStorage.CODEC).orElseThrow();
                addStorage(storage, pos);
            });

            view.childrenListOrEmpty("fluids").forEach(fluid -> {
                BlockPos pos = fluid.read("pos", BlockPos.CODEC).orElseThrow();
                MountedFluidStorage storage = fluid.read("storage", MountedFluidStorage.CODEC).orElseThrow();
                addStorage(storage, pos);
            });

            view.read("interactable_positions", CreateCodecs.BLOCK_POS_LIST_CODEC).ifPresent(list -> interactablePositions = new HashSet<>(list));
        } catch (Throwable t) {
            Create.LOGGER.error("Error deserializing mounted storage", t);
            // an exception will leave the manager in an invalid state, initialize must be called
        }

        initialize();
        afterSync(clientPacket, contraption);
    }

    public <T> void read(final DynamicOps<T> ops, MapLike<T> map, boolean clientPacket, @Nullable Contraption contraption) {
        reset();

        try {
            ops.getList(map.get("items")).getOrThrow().accept(item -> {
                MapLike<T> data = ops.getMap(item).getOrThrow();
                BlockPos pos = BlockPos.CODEC.parse(ops, data.get("pos")).getOrThrow();
                MountedItemStorage storage = MountedItemStorage.CODEC.parse(ops, data.get("storage")).getOrThrow();
                addStorage(storage, pos);
            });

            ops.getList(map.get("fluids")).getOrThrow().accept(fluid -> {
                MapLike<T> data = ops.getMap(fluid).getOrThrow();
                BlockPos pos = BlockPos.CODEC.parse(ops, data.get("pos")).getOrThrow();
                MountedFluidStorage storage = MountedFluidStorage.CODEC.parse(ops, data.get("storage")).getOrThrow();
                addStorage(storage, pos);
            });

            CreateCodecs.BLOCK_POS_LIST_CODEC.parse(ops, map.get("interactable_positions"))
                .ifSuccess(list -> interactablePositions = new HashSet<>(list));
        } catch (Throwable t) {
            Create.LOGGER.error("Error deserializing mounted storage", t);
            // an exception will leave the manager in an invalid state, initialize must be called
        }

        initialize();
        afterSync(clientPacket, contraption);
    }

    private void afterSync(boolean clientPacket, @Nullable Contraption contraption) {
        // for client sync, run initial afterSync callbacks
        if (!clientPacket || contraption == null)
            return;

        getAllItemStorages().forEach((pos, storage) -> {
            if (storage instanceof SyncedMountedStorage synced) {
                synced.afterSync(contraption, pos);
            }
        });
        getFluids().storages.forEach((pos, storage) -> {
            if (storage instanceof SyncedMountedStorage synced) {
                synced.afterSync(contraption, pos);
            }
        });
    }

    public void write(ValueOutput view, boolean clientPacket) {
        ValueOutput.ValueOutputList items = view.childrenList("items");
        getAllItemStorages().forEach((pos, storage) -> {
            if (!clientPacket || storage instanceof SyncedMountedStorage) {
                ValueOutput item = items.addChild();
                item.store("pos", BlockPos.CODEC, pos);
                item.store("storage", MountedItemStorage.CODEC, storage);
            }
        });

        ValueOutput.ValueOutputList fluids = view.childrenList("fluids");
        getFluids().storages.forEach((pos, storage) -> {
            if (!clientPacket || storage instanceof SyncedMountedStorage) {
                ValueOutput fluid = fluids.addChild();
                fluid.store("pos", BlockPos.CODEC, pos);
                fluid.store("storage", MountedFluidStorage.CODEC, storage);
            }
        });

        if (clientPacket) {
            // let the client know of all non-synced ones too
            List<BlockPos> list = Sets.union(this.getAllItemStorages().keySet(), getFluids().storages.keySet()).stream().toList();
            view.store("interactable_positions", CreateCodecs.BLOCK_POS_LIST_CODEC, list);
        }
    }

    public <T> void write(final DynamicOps<T> ops, final T empty, RecordBuilder<T> map, boolean clientPacket) {
        ListBuilder<T> items = ops.listBuilder();
        getAllItemStorages().forEach((pos, storage) -> {
            if (!clientPacket || storage instanceof SyncedMountedStorage) {
                RecordBuilder<T> item = ops.mapBuilder();
                item.add("pos", pos, BlockPos.CODEC);
                item.add("storage", storage, MountedItemStorage.CODEC);
                items.add(item.build(empty));
            }
        });
        map.add("items", items.build(empty));

        ListBuilder<T> fluids = ops.listBuilder();
        getFluids().storages.forEach((pos, storage) -> {
            if (!clientPacket || storage instanceof SyncedMountedStorage) {
                RecordBuilder<T> fluid = ops.mapBuilder();
                fluid.add("pos", pos, BlockPos.CODEC);
                fluid.add("storage", storage, MountedFluidStorage.CODEC);
                fluids.add(fluid.build(empty));
            }
        });
        map.add("fluids", fluids.build(empty));

        if (clientPacket) {
            // let the client know of all non-synced ones too
            List<BlockPos> list = Sets.union(this.getAllItemStorages().keySet(), getFluids().storages.keySet()).stream().toList();
            map.add("interactable_positions", list, CreateCodecs.BLOCK_POS_LIST_CODEC);
        }
    }

    public void attachExternal(Container externalStorage) {
        this.externalHandlers.add(externalStorage);
        int size = externalHandlers.size();
        Container[] all = new Container[size + 1];
        all[0] = this.items;
        for (int i = 0; i < size; i++) {
            all[i + 1] = externalHandlers.get(i);
        }

        this.allItems = new CombinedInvWrapper(all);
    }

    /**
     * The primary way to access a contraption's inventory. Includes all
     * non-internal mounted storages as well as all external storage.
     */
    public CombinedInvWrapper getAllItems() {
        this.assertInitialized();
        return this.allItems;
    }

    /**
     * Gets a map of all MountedItemStorages in the contraption, irrelevant of them being internal or providing fuel.
     */
    public ImmutableMap<BlockPos, MountedItemStorage> getAllItemStorages() {
        this.assertInitialized();
        return this.allItemStorages;
    }

    /**
     * Gets an item handler wrapping all non-internal mounted storages. This is not
     * the whole contraption inventory as it does not include external storages.
     * Most often, you want {@link #getAllItems()}, which does.
     */
    public MountedItemStorageWrapper getMountedItems() {
        this.assertInitialized();
        return this.items;
    }

    /**
     * Gets an item handler wrapping all non-internal mounted storages that provide fuel.
     * May be null if none are present.
     */
    @Nullable
    public MountedItemStorageWrapper getFuelItems() {
        this.assertInitialized();
        return this.fuelItems;
    }

    /**
     * Gets a fluid handler wrapping all mounted fluid storages.
     */
    public MountedFluidStorageWrapper getFluids() {
        this.assertInitialized();
        return this.fluids;
    }

    public boolean handlePlayerStorageInteraction(Contraption contraption, Player player, BlockPos localPos) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return this.interactablePositions != null && this.interactablePositions.contains(localPos);
        }

        StructureBlockInfo info = contraption.getBlocks().get(localPos);
        if (info == null)
            return false;

        MountedStorageManager storageManager = contraption.getStorage();
        MountedItemStorage storage = storageManager.getAllItemStorages().get(localPos);

        if (storage != null) {
            return storage.handleInteraction(serverPlayer, contraption, info);
        } else {
            return false;
        }
    }

    private void readLegacy(HolderLookup.Provider registries, CompoundTag nbt) {
        NBTHelper.iterateCompoundList(
            nbt.getListOrEmpty("Storage"), tag -> {
                BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
                CompoundTag data = tag.getCompoundOrEmpty("Data");

                //TODO
                //                if (data.contains("Toolbox")) {
                //                    this.addStorage(ToolboxMountedStorage.fromLegacy(registries, data), pos);
                //                } else if (data.contains("NoFuel")) {
                //                    this.addStorage(ItemVaultMountedStorage.fromLegacy(registries, data), pos);
                //                } else if (data.contains("Bottomless")) {
                //                    ItemStack supplied = data.getCompound("ProvidedStack").flatMap(c -> ItemStack.fromNbt(registries, c)).orElse(ItemStack.EMPTY);
                //                    this.addStorage(new CreativeCrateMountedStorage(supplied), pos);
                //                } else if (data.contains("Synced")) {
                //                    this.addStorage(DepotMountedStorage.fromLegacy(registries, data), pos);
                //                } else {
                //                    // we can create a fallback storage safely, it will be validated before unmounting
                //                    //                    ItemStackHandler handler = new ItemStackHandler();
                //                    //                    handler.deserializeNBT(registries, data);
                //                    this.addStorage(new FallbackMountedStorage(new Object()), pos);
                //                }
            }
        );

        NBTHelper.iterateCompoundList(
            nbt.getListOrEmpty("FluidStorage"), tag -> {
                BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
                CompoundTag data = tag.getCompoundOrEmpty("Data");

                //TODO
                //                if (data.contains("Bottomless")) {
                //                    this.addStorage(CreativeFluidTankMountedStorage.fromLegacy(registries, data), pos);
                //                } else {
                //                    this.addStorage(FluidTankMountedStorage.fromLegacy(registries, data), pos);
                //                }
            }
        );
    }

    private void addStorage(MountedItemStorage storage, BlockPos pos) {
        this.itemsBuilder.put(pos, storage);
        if (storage instanceof SyncedMountedStorage synced)
            this.syncedItemsBuilder.put(pos, synced);
    }

    private void addStorage(MountedFluidStorage storage, BlockPos pos) {
        this.fluidsBuilder.put(pos, storage);
        if (storage instanceof SyncedMountedStorage synced)
            this.syncedFluidsBuilder.put(pos, synced);
    }

    private static <K, V> ImmutableMap<K, V> subMap(Map<K, V> map, Predicate<V> predicate) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        map.forEach((key, value) -> {
            if (predicate.test(value)) {
                builder.put(key, value);
            }
        });
        return builder.build();
    }
}
