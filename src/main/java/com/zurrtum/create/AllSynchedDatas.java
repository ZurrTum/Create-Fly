package com.zurrtum.create;

import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import com.zurrtum.create.content.trains.entity.CarriageSyncData;
import com.zurrtum.create.content.trains.entity.CarriageSyncDataSerializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.*;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.util.ClassTreeIdRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.function.*;

public class AllSynchedDatas {
    private static final Map<Class<?>, SynchedData> ALL = new IdentityHashMap<>();
    private static final Map<Class<?>, Optional<SynchedData>> ON_DATA = new IdentityHashMap<>();
    public static final List<EntityDataSerializer<?>> HANDLERS = new ArrayList<>();
    public static final EntityDataSerializer<Optional<MinecartController>> MINECART_CONTROLLER_HANDLER = register(MinecartController.PACKET_CODEC.apply(
        ByteBufCodecs::optional));
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID_HANDLER = register(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional));
    public static final EntityDataSerializer<Optional<List<ItemStack>>> CAPTURE_DROPS_HANDLER = register(ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list())
        .apply(ByteBufCodecs::optional));
    public static final EntityDataSerializer<CarriageSyncData> CARRIAGE_DATA_HANDLER = register(CarriageSyncDataSerializer::new);
    public static final EntityDataSerializer<Optional<Vec3>> OPTIONAL_VEC3D_HANDLER = register(Vec3.STREAM_CODEC.apply(ByteBufCodecs::optional));
    public static final EntityDataSerializer<CompoundTag> NBT_COMPOUND_HANDLER = register(ByteBufCodecs.TRUSTED_COMPOUND_TAG);
    public static final Entry<Integer> HAUNTING = register(Horse.class, EntityDataSerializers.INT, 0);
    public static final Entry<String> ITEM_TYPE = register(ItemEntity.class, EntityDataSerializers.STRING, "");
    public static final Entry<Integer> ITEM_TIME = register(ItemEntity.class, EntityDataSerializers.INT, 0);
    public static final Entry<Optional<BlockPos>> BYPASS_CRUSHING_WHEEL = register(
        ItemEntity.class,
        EntityDataSerializers.OPTIONAL_BLOCK_POS,
        Optional.empty()
    );
    public static final Entry<Optional<MinecartController>> MINECART_CONTROLLER = register(
        AbstractMinecart.class,
        MINECART_CONTROLLER_HANDLER,
        Optional.empty(),
        (entity, value) -> value.ifPresent(controller -> controller.setCart(entity))
    );
    public static final Entry<Integer> VISUAL_BACKTANK_AIR = register(Player.class, EntityDataSerializers.INT, 0);
    public static final Entry<Boolean> FIRE_IMMUNE = register(Player.class, EntityDataSerializers.BOOLEAN, false);
    public static final Entry<Boolean> HEAVY_BOOTS = register(Player.class, EntityDataSerializers.BOOLEAN, false);
    public static final Entry<Boolean> CRUSH_DROP = register(Entity.class, EntityDataSerializers.BOOLEAN, false);
    public static final Entry<Optional<List<ItemStack>>> CAPTURE_DROPS = register(Entity.class, CAPTURE_DROPS_HANDLER, Optional.empty());
    public static final Entry<Boolean> CONTRAPTION_GROUNDED = register(Entity.class, EntityDataSerializers.BOOLEAN, false);
    public static final Entry<Optional<Vec3>> CONTRAPTION_DISMOUNT_LOCATION = register(LivingEntity.class, OPTIONAL_VEC3D_HANDLER, Optional.empty());
    public static final Entry<Optional<Vec3>> CONTRAPTION_MOUNT_LOCATION = register(Player.class, OPTIONAL_VEC3D_HANDLER, Optional.empty());
    public static final Entry<Boolean> IS_USING_LECTERN_CONTROLLER = register(Player.class, EntityDataSerializers.BOOLEAN, false);
    public static final Entry<CompoundTag> TOOLBOX = register(Player.class, NBT_COMPOUND_HANDLER, new CompoundTag());
    public static final Entry<Integer> LAST_OVERRIDE_LIMB_SWING_UPDATE = register(Player.class, EntityDataSerializers.INT, -1);
    public static final Entry<Float> OVERRIDE_LIMB_SWING = register(Player.class, EntityDataSerializers.FLOAT, 0F);
    public static final Entry<Boolean> PARROT_TRAIN_HAT = register(Parrot.class, EntityDataSerializers.BOOLEAN, false);

    private static <T> Entry<T> register(Class<? extends SyncedDataHolder> type, EntityDataSerializer<T> handler, T def) {
        return register(type, handler, def, null);
    }

    private static <E extends SyncedDataHolder, T> Entry<T> register(Class<E> type, EntityDataSerializer<T> handler, T def, BiConsumer<E, T> onData) {
        return ALL.computeIfAbsent(type, SynchedData::new).add(handler, def, onData);
    }

    private static <T> EntityDataSerializer<T> register(StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        EntityDataSerializer<T> handler = EntityDataSerializer.forValueType(codec);
        HANDLERS.add(handler);
        return handler;
    }

    private static <T> EntityDataSerializer<T> register(Supplier<EntityDataSerializer<T>> factory) {
        EntityDataSerializer<T> handler = factory.get();
        HANDLERS.add(handler);
        return handler;
    }

    public static SynchedData get(Class<?> type) {
        return ALL.computeIfAbsent(type, SynchedData::new);
    }

    public static void onData(SyncedDataHolder entity, DataValue<?> entry) {
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
        private List<Consumer<SynchedEntityData.DataItem<?>[]>> actions;
        private Int2ObjectMap<BiConsumer<? extends SyncedDataHolder, ?>> listeners;
        private int size;

        public SynchedData(Class<?> type) {
            this.type = type;
        }

        public <E extends SyncedDataHolder, T> Entry<T> add(EntityDataSerializer<T> handler, T def, BiConsumer<E, T> onData) {
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

        public int preparse(ClassTreeIdRegistry map, BiFunction<ClassTreeIdRegistry, Class<?>, Integer> factory) {
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

        public void register(SynchedEntityData.DataItem<?>[] entries) {
            actions.forEach(action -> action.accept(entries));
        }

        @SuppressWarnings("unchecked")
        protected <E extends SyncedDataHolder, T> void onData(E entity, DataValue<T> serializedEntry) {
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
        private final EntityDataSerializer<T> handler;
        private final Supplier<T> def;
        private final BiConsumer<? extends SyncedDataHolder, T> listener;
        private BiFunction<Class<?>, Integer, Consumer<SynchedEntityData.DataItem<?>[]>> add = this::firstAdd;
        private Function<Entity, T> get;
        private TriConsumer<Entity, T, Boolean> set;

        @SuppressWarnings("unchecked")
        private Entry(EntityDataSerializer<T> handler, T def, BiConsumer<? extends SyncedDataHolder, T> listener) {
            this.handler = handler;
            if (def instanceof CompoundTag nbt) {
                this.def = () -> (T) nbt.copy();
            } else {
                this.def = () -> def;
            }
            this.listener = listener;
        }

        private Consumer<SynchedEntityData.DataItem<?>[]> firstAdd(Class<?> type, int id) {
            EntityDataAccessor<T> data = handler.createAccessor(id);
            get = target -> target.getEntityData().get(data);
            set = (target, value, force) -> target.getEntityData().set(data, value, force);
            add = (otherType, otherId) -> {
                IdentityHashMap<Class<?>, EntityDataAccessor<T>> map = new IdentityHashMap<>();
                map.put(type, data);
                get = target -> target.getEntityData().get(map.get(target.getClass()));
                set = (target, value, force) -> target.getEntityData().set(map.get(target.getClass()), value, force);
                add = (addType, addId) -> {
                    EntityDataAccessor<T> addData = handler.createAccessor(addId);
                    map.put(addType, addData);
                    return entries -> entries[addId] = new SynchedEntityData.DataItem<>(addData, def.get());
                };
                return add.apply(otherType, otherId);
            };
            return entries -> entries[id] = new SynchedEntityData.DataItem<>(data, def.get());
        }

        public Consumer<SynchedEntityData.DataItem<?>[]> add(Class<?> type, int id) {
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
