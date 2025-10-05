package com.zurrtum.create;

import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import com.zurrtum.create.content.trains.entity.CarriageSyncData;
import com.zurrtum.create.content.trains.entity.CarriageSyncDataSerializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.*;
import net.minecraft.entity.data.DataTracker.SerializedEntry;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.Class2IntMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.function.*;

public class AllSynchedDatas {
    private static final Map<Class<?>, SynchedData> ALL = new IdentityHashMap<>();
    private static final Map<Class<?>, Optional<SynchedData>> ON_DATA = new IdentityHashMap<>();
    public static final List<TrackedDataHandler<?>> HANDLERS = new ArrayList<>();
    public static final TrackedDataHandler<Optional<MinecartController>> MINECART_CONTROLLER_HANDLER = register(MinecartController.PACKET_CODEC.collect(
        PacketCodecs::optional));
    public static final TrackedDataHandler<Optional<UUID>> OPTIONAL_UUID_HANDLER = register(Uuids.PACKET_CODEC.collect(PacketCodecs::optional));
    public static final TrackedDataHandler<Optional<List<ItemStack>>> CAPTURE_DROPS_HANDLER = register(ItemStack.PACKET_CODEC.collect(PacketCodecs.toList())
        .collect(PacketCodecs::optional));
    public static final TrackedDataHandler<CarriageSyncData> CARRIAGE_DATA_HANDLER = register(CarriageSyncDataSerializer::new);
    public static final TrackedDataHandler<Optional<Vec3d>> OPTIONAL_VEC3D_HANDLER = register(Vec3d.PACKET_CODEC.collect(PacketCodecs::optional));
    public static final TrackedDataHandler<NbtCompound> NBT_COMPOUND_HANDLER = register(PacketCodecs.UNLIMITED_NBT_COMPOUND);
    public static final Entry<Integer> HAUNTING = register(HorseEntity.class, TrackedDataHandlerRegistry.INTEGER, 0);
    public static final Entry<String> ITEM_TYPE = register(ItemEntity.class, TrackedDataHandlerRegistry.STRING, "");
    public static final Entry<Integer> ITEM_TIME = register(ItemEntity.class, TrackedDataHandlerRegistry.INTEGER, 0);
    public static final Entry<Optional<BlockPos>> BYPASS_CRUSHING_WHEEL = register(
        ItemEntity.class,
        TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS,
        Optional.empty()
    );
    public static final Entry<Optional<MinecartController>> MINECART_CONTROLLER = register(
        AbstractMinecartEntity.class,
        MINECART_CONTROLLER_HANDLER,
        Optional.empty(),
        (entity, value) -> value.ifPresent(controller -> controller.setCart(entity))
    );
    public static final Entry<Integer> VISUAL_BACKTANK_AIR = register(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER, 0);
    public static final Entry<Boolean> FIRE_IMMUNE = register(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN, false);
    public static final Entry<Boolean> HEAVY_BOOTS = register(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN, false);
    public static final Entry<Boolean> CRUSH_DROP = register(Entity.class, TrackedDataHandlerRegistry.BOOLEAN, false);
    public static final Entry<Optional<List<ItemStack>>> CAPTURE_DROPS = register(Entity.class, CAPTURE_DROPS_HANDLER, Optional.empty());
    public static final Entry<Boolean> CONTRAPTION_GROUNDED = register(Entity.class, TrackedDataHandlerRegistry.BOOLEAN, false);
    public static final Entry<Optional<Vec3d>> CONTRAPTION_DISMOUNT_LOCATION = register(LivingEntity.class, OPTIONAL_VEC3D_HANDLER, Optional.empty());
    public static final Entry<Optional<Vec3d>> CONTRAPTION_MOUNT_LOCATION = register(PlayerEntity.class, OPTIONAL_VEC3D_HANDLER, Optional.empty());
    public static final Entry<Boolean> IS_USING_LECTERN_CONTROLLER = register(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN, false);
    public static final Entry<NbtCompound> TOOLBOX = register(PlayerEntity.class, NBT_COMPOUND_HANDLER, new NbtCompound());
    public static final Entry<Integer> LAST_OVERRIDE_LIMB_SWING_UPDATE = register(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER, -1);
    public static final Entry<Float> OVERRIDE_LIMB_SWING = register(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT, 0F);
    public static final Entry<Boolean> PARROT_TRAIN_HAT = register(ParrotEntity.class, TrackedDataHandlerRegistry.BOOLEAN, false);

    private static <T> Entry<T> register(Class<? extends DataTracked> type, TrackedDataHandler<T> handler, T def) {
        return register(type, handler, def, null);
    }

    private static <E extends DataTracked, T> Entry<T> register(Class<E> type, TrackedDataHandler<T> handler, T def, BiConsumer<E, T> onData) {
        return ALL.computeIfAbsent(type, SynchedData::new).add(handler, def, onData);
    }

    private static <T> TrackedDataHandler<T> register(PacketCodec<? super RegistryByteBuf, T> codec) {
        TrackedDataHandler<T> handler = TrackedDataHandler.create(codec);
        HANDLERS.add(handler);
        return handler;
    }

    private static <T> TrackedDataHandler<T> register(Supplier<TrackedDataHandler<T>> factory) {
        TrackedDataHandler<T> handler = factory.get();
        HANDLERS.add(handler);
        return handler;
    }

    public static SynchedData get(Class<?> type) {
        return ALL.computeIfAbsent(type, SynchedData::new);
    }

    public static void onData(DataTracked entity, SerializedEntry<?> entry) {
        ON_DATA.computeIfAbsent(entity.getClass(), AllSynchedDatas::getParentOrEmpty).ifPresent(data -> data.onData(entity, entry));
    }

    private static Optional<SynchedData> getParentOrEmpty(Class<?> type) {
        while (type != Entity.class) {
            type = type.getSuperclass();
            Optional<SynchedData> value = ON_DATA.get(type);
            if (value != null) {
                return value;
            }
        }
        return Optional.empty();
    }

    public static void register() {
    }

    public static class SynchedData {
        private final Class<?> type;
        private final Deque<Entry<?>> datas = new ArrayDeque<>();
        private List<Consumer<DataTracker.Entry<?>[]>> actions;
        private Int2ObjectMap<BiConsumer<? extends DataTracked, ?>> listeners;
        private int size;

        public SynchedData(Class<?> type) {
            this.type = type;
        }

        public <E extends DataTracked, T> Entry<T> add(TrackedDataHandler<T> handler, T def, BiConsumer<E, T> onData) {
            Entry<T> entry = new Entry<>(handler, def, onData);
            datas.add(entry);
            return entry;
        }

        private void preparse(int index, SynchedData parent) {
            if (parent != null) {
                if (datas.isEmpty()) {
                    datas.addAll(parent.datas);
                } else {
                    Iterator<Entry<?>> iterator = parent.datas.descendingIterator();
                    while (iterator.hasNext()) {
                        datas.addFirst(iterator.next());
                    }
                }
            }
            actions = new ArrayList<>(datas.size());
            for (Entry<?> task : datas) {
                if (task.listener != null) {
                    if (listeners == null) {
                        listeners = new Int2ObjectOpenHashMap<>();
                        ON_DATA.put(type, Optional.of(this));
                    }
                    listeners.put(index, task.listener);
                }
                actions.add(task.add(type, index++));
            }
            size = index;
        }

        public int preparse(Class2IntMap map, BiFunction<Class2IntMap, Class<?>, Integer> factory) {
            if (size != 0) {
                return size;
            }
            Deque<SynchedData> parents = new ArrayDeque<>();
            SynchedData parent = null;
            Class<?> key = type;
            while (key != Entity.class) {
                key = key.getSuperclass();
                SynchedData data = ALL.get(key);
                if (data != null) {
                    if (data.size != 0) {
                        parent = data;
                        break;
                    }
                    parents.addFirst(data);
                }
            }
            while (!parents.isEmpty()) {
                SynchedData data = parents.pollFirst();
                data.preparse(factory.apply(map, data.type), parent);
                parent = data;
            }
            preparse(factory.apply(map, type), parent);
            return size;
        }

        public void register(DataTracker.Entry<?>[] entries) {
            actions.forEach(action -> action.accept(entries));
        }

        @SuppressWarnings("unchecked")
        protected <E extends DataTracked, T> void onData(E entity, SerializedEntry<T> serializedEntry) {
            if (listeners == null) {
                return;
            }
            BiConsumer<E, T> callback = (BiConsumer<E, T>) listeners.get(serializedEntry.id());
            if (callback != null) {
                callback.accept(entity, serializedEntry.value());
            }
        }
    }

    public static class Entry<T> {
        private final TrackedDataHandler<T> handler;
        private final Supplier<T> def;
        private final BiConsumer<? extends DataTracked, T> listener;
        private BiFunction<Class<?>, Integer, Consumer<DataTracker.Entry<?>[]>> add = this::firstAdd;
        private Function<Entity, T> get;
        private TriConsumer<Entity, T, Boolean> set;

        @SuppressWarnings("unchecked")
        private Entry(TrackedDataHandler<T> handler, T def, BiConsumer<? extends DataTracked, T> listener) {
            this.handler = handler;
            if (def instanceof NbtCompound nbt) {
                this.def = () -> (T) nbt.copy();
            } else {
                this.def = () -> def;
            }
            this.listener = listener;
        }

        private Consumer<DataTracker.Entry<?>[]> firstAdd(Class<?> type, int id) {
            TrackedData<T> data = handler.create(id);
            get = target -> target.getDataTracker().get(data);
            set = (target, value, force) -> target.getDataTracker().set(data, value, force);
            add = (otherType, otherId) -> {
                IdentityHashMap<Class<?>, TrackedData<T>> map = new IdentityHashMap<>();
                map.put(type, data);
                get = target -> target.getDataTracker().get(map.get(target.getClass()));
                set = (target, value, force) -> target.getDataTracker().set(map.get(target.getClass()), value, force);
                add = (addType, addId) -> {
                    TrackedData<T> addData = handler.create(addId);
                    map.put(addType, addData);
                    return entries -> entries[addId] = new DataTracker.Entry<>(addData, def.get());
                };
                return add.apply(otherType, otherId);
            };
            return entries -> entries[id] = new DataTracker.Entry<>(data, def.get());
        }

        public Consumer<DataTracker.Entry<?>[]> add(Class<?> type, int id) {
            return add.apply(type, id);
        }

        public T get(Entity target) {
            return get.apply(target);
        }

        public void set(Entity target, T value) {
            set.accept(target, value, false);
        }

        public void set(Entity target, T value, boolean force) {
            set.accept(target, value, force);
        }
    }
}
