package com.zurrtum.create.foundation.codec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.*;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.foundation.item.ItemSlots;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Function;

public class CreateCodecs {
    public static final Codec<Integer> INT_STR = Codec.STRING.comapFlatMap(
        string -> {
            try {
                return DataResult.success(Integer.parseInt(string));
            } catch (NumberFormatException ignored) {
                return DataResult.error(() -> "Not an integer: " + string);
            }
        }, String::valueOf
    );

    public static final Codec<ItemStackHandler> ITEM_STACK_HANDLER = Codec.lazyInitialized(() -> ItemSlots.CODEC.xmap(
        slots -> slots.toHandler(
            ItemStackHandler::new), ItemSlots::fromHandler
    ));

    public static Codec<Integer> boundedIntStr(int min) {
        return INT_STR.validate(i -> i >= min ? DataResult.success(i) : DataResult.error(() -> "Value under minimum of " + min));
    }

    public static final Codec<Double> NON_NEGATIVE_DOUBLE = doubleRangeWithMessage(0, Double.MAX_VALUE, i -> "Value must be non-negative: " + i);
    public static final Codec<Double> POSITIVE_DOUBLE = doubleRangeWithMessage(1, Double.MAX_VALUE, i -> "Value must be positive: " + i);

    private static Codec<Double> doubleRangeWithMessage(double min, double max, Function<Double, String> errorMessage) {
        return Codec.DOUBLE.validate(i -> i.compareTo(min) >= 0 && i.compareTo(max) <= 0 ? DataResult.success(i) : DataResult.error(() -> errorMessage.apply(
            i)));
    }

    public static final Codec<BlockEntityType<?>> BLOCK_ENTITY_TYPE_CODEC = BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec();
    public static final Codec<List<TransportedItemStack>> TRANSPORTED_ITEM_LIST_CODEC = TransportedItemStack.CODEC.listOf();
    public static final Codec<List<ItemStack>> ITEM_LIST_CODEC = ItemStack.OPTIONAL_CODEC.listOf();
    public static final Codec<List<FluidStack>> FLUID_LIST_CODEC = FluidStack.OPTIONAL_CODEC.listOf();
    public static final Codec<List<Direction>> DIRECTION_LIST_CODEC = Direction.CODEC.listOf();
    public static final Codec<List<BlockState>> BLOCK_STATE_LIST_CODEC = BlockState.CODEC.listOf();
    public static final Codec<List<BlockPos>> BLOCK_POS_LIST_CODEC = BlockPos.CODEC.listOf();
    public static final Codec<Set<BlockPos>> BLOCKPOS_SET_CODEC = BlockPos.CODEC.listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
    public static final Codec<List<Float>> FLOAT_LIST_CODEC = Codec.FLOAT.listOf();
    public static final Codec<List<Pair<BlockPos, Direction>>> BLOCK_POS_DIRECTION_LIST_CODEC = Pair.codec(BlockPos.CODEC, Direction.CODEC).listOf();
    public static final Codec<AABB> BOX_CODEC = Codec.DOUBLE.listOf()
        .xmap(
            data -> new AABB(data.get(0), data.get(1), data.get(2), data.get(3), data.get(4), data.get(5)),
            box -> List.of(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
        );
    public static final Codec<ListTag> NBT_LIST_CODEC = Codec.PASSTHROUGH.comapFlatMap(
        dynamic -> {
            Tag nbtElement = dynamic.convert(NbtOps.INSTANCE).getValue();
            return nbtElement instanceof ListTag nbtList ? DataResult.success(nbtList == dynamic.getValue() ? nbtList.copy() : nbtList) : DataResult.error(
                () -> "Not a list tag: " + nbtElement);
        }, nbt -> new Dynamic<>(NbtOps.INSTANCE, nbt.copy())
    );
    public static final Codec<Block> BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec();
    public static final Codec<Map<BlockPos, Boolean>> BLOCK_POS_BOOLEAN_MAP_CODEC = getCodecMap(BlockPos.CODEC, Codec.BOOL);
    public static final Codec<Couple<BlockPos>> COUPLE_BLOCK_POS_CODEC = Couple.codec(BlockPos.CODEC);
    public static final Codec<Couple<Vec3>> COUPLE_VEC3D_CODEC = Couple.codec(Vec3.CODEC);
    public static final Codec<Couple<Integer>> COUPLE_INT_CODEC = Couple.codec(Codec.INT);
    public static final Codec<Set<UUID>> UUID_SET_CODEC = UUIDUtil.CODEC.listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
    public static final Codec<List<CompoundTag>> NBT_COMPOUND_LIST_CODEC = CompoundTag.CODEC.listOf();

    public static <K, V> Codec<Map<K, V>> getCodecMap(Codec<K> keyCodec, Codec<V> valueCodec) {
        return new Codec<>() {
            @Override
            public <T> DataResult<com.mojang.datafixers.util.Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
                MapLike<T> map = ops.getMap(input).getOrThrow();
                Iterator<T> keys = ops.getStream(map.get("Keys")).getOrThrow().iterator();
                Iterator<T> values = ops.getStream(map.get("Values")).getOrThrow().iterator();
                HashMap<K, V> result = new HashMap<>();
                while (keys.hasNext()) {
                    result.put(keyCodec.parse(ops, keys.next()).getOrThrow(), valueCodec.parse(ops, values.next()).getOrThrow());
                }
                return DataResult.success(com.mojang.datafixers.util.Pair.of(result, ops.empty()));
            }

            @Override
            public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
                RecordBuilder<T> builder = ops.mapBuilder();
                ListBuilder<T> keys = ops.listBuilder();
                ListBuilder<T> values = ops.listBuilder();
                input.forEach((key, value) -> {
                    keys.add(keyCodec.encodeStart(ops, key));
                    values.add(valueCodec.encodeStart(ops, value));
                });
                builder.add("Keys", keys.build(ops.empty()));
                builder.add("Values", values.build(ops.empty()));
                return builder.build(prefix);
            }
        };
    }

    public static <V> Codec<List<V>> getArrayListCodec(Codec<V> codec) {
        return new Codec<>() {
            @Override
            public <T> DataResult<com.mojang.datafixers.util.Pair<List<V>, T>> decode(DynamicOps<T> ops, T input) {
                List<V> list = new ArrayList<>();
                ops.getList(input).ifSuccess(consumer -> consumer.accept(item -> codec.parse(ops, item).ifSuccess(list::add)));
                return DataResult.success(com.mojang.datafixers.util.Pair.of(list, ops.empty()));
            }

            @Override
            public <T> DataResult<T> encode(List<V> input, DynamicOps<T> ops, T prefix) {
                ListBuilder<T> list = ops.listBuilder();
                for (V item : input) {
                    list.add(item, codec);
                }
                return list.build(prefix);
            }
        };
    }
}
