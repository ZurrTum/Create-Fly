package com.zurrtum.create.catnip.nbt;

import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class NBTHelper {

    public static void putMarker(NbtCompound nbt, String marker) {
        nbt.putBoolean(marker, true);
    }

    // Backwards compatible with 1.20
    public static BlockPos readBlockPos(NbtCompound nbt, String key) {
        BlockPos pos = nbt.get(key, BlockPos.CODEC).orElse(null);
        if (pos != null)
            return pos;
        NbtCompound oldTag = nbt.getCompoundOrEmpty(key);
        return new BlockPos(oldTag.getInt("X", 0), oldTag.getInt("Y", 0), oldTag.getInt("Z", 0));
    }

    public static <T extends Enum<?>> T readEnum(NbtCompound nbt, String key, Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null)
            throw new IllegalArgumentException("Non-Enum class passed to readEnum: " + enumClass.getName());
        if (nbt.contains(key)) {
            String name = nbt.getString(key, "");
            for (T t : enumConstants) {
                if (t.name().equals(name))
                    return t;
            }
        }
        return enumConstants[0];
    }

    public static <T extends Enum<?>> void writeEnum(NbtCompound nbt, String key, T enumConstant) {
        nbt.putString(key, enumConstant.name());
    }

    public static <T> NbtList writeCompoundList(Iterable<T> list, Function<T, NbtCompound> serializer) {
        NbtList listNBT = new NbtList();
        list.forEach(t -> {
            NbtCompound apply = serializer.apply(t);
            if (apply == null)
                return;
            listNBT.add(apply);
        });
        return listNBT;
    }

    public static <T> List<T> readCompoundList(NbtList listNBT, Function<NbtCompound, T> deserializer) {
        List<T> list = new ArrayList<>(listNBT.size());
        listNBT.forEach(inbt -> list.add(deserializer.apply((NbtCompound) inbt)));
        return list;
    }

    public static void iterateCompoundList(NbtList listNBT, Consumer<NbtCompound> consumer) {
        listNBT.forEach(inbt -> consumer.accept((NbtCompound) inbt));
    }

    public static NbtList writeAABB(Box bb) {
        NbtList bbtag = new NbtList();
        bbtag.add(NbtFloat.of((float) bb.minX));
        bbtag.add(NbtFloat.of((float) bb.minY));
        bbtag.add(NbtFloat.of((float) bb.minZ));
        bbtag.add(NbtFloat.of((float) bb.maxX));
        bbtag.add(NbtFloat.of((float) bb.maxY));
        bbtag.add(NbtFloat.of((float) bb.maxZ));
        return bbtag;
    }

    @Nullable
    public static Box readAABB(NbtList bbTag) {
        if (bbTag.isEmpty())
            return null;
        return new Box(
            bbTag.getFloat(0, 0),
            bbTag.getFloat(1, 0),
            bbTag.getFloat(2, 0),
            bbTag.getFloat(3, 0),
            bbTag.getFloat(4, 0),
            bbTag.getFloat(5, 0)
        );
    }

    public static NbtList writeVec3i(Vec3i vec) {
        NbtList tag = new NbtList();
        tag.add(NbtInt.of(vec.getX()));
        tag.add(NbtInt.of(vec.getY()));
        tag.add(NbtInt.of(vec.getZ()));
        return tag;
    }

    public static Vec3i readVec3i(NbtList tag) {
        return new Vec3i(tag.getInt(0, 0), tag.getInt(1, 0), tag.getInt(2, 0));
    }

    @NotNull
    public static NbtElement getINBT(NbtCompound nbt, String id) {
        NbtElement inbt = nbt.get(id);
        if (inbt != null)
            return inbt;
        return new NbtCompound();
    }

    public static NbtCompound intToCompound(int i) {
        NbtCompound compoundTag = new NbtCompound();
        compoundTag.putInt("V", i);
        return compoundTag;
    }

    public static int intFromCompound(NbtCompound compoundTag) {
        return compoundTag.getInt("V", 0);
    }

    public static void writeResourceLocation(NbtCompound nbt, String key, Identifier location) {
        nbt.putString(key, location.toString());
    }

    public static Identifier readResourceLocation(NbtCompound nbt, String key) {
        return Identifier.of(nbt.getString(key, ""));
    }

}