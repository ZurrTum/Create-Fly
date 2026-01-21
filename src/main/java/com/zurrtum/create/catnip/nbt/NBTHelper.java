package com.zurrtum.create.catnip.nbt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class NBTHelper {

    public static void putMarker(CompoundTag nbt, String marker) {
        nbt.putBoolean(marker, true);
    }

    // Backwards compatible with 1.20
    public static BlockPos readBlockPos(CompoundTag nbt, String key) {
        BlockPos pos = nbt.read(key, BlockPos.CODEC).orElse(null);
        if (pos != null)
            return pos;
        CompoundTag oldTag = nbt.getCompoundOrEmpty(key);
        return new BlockPos(oldTag.getIntOr("X", 0), oldTag.getIntOr("Y", 0), oldTag.getIntOr("Z", 0));
    }

    public static <T extends Enum<?>> T readEnum(CompoundTag nbt, String key, Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null)
            throw new IllegalArgumentException("Non-Enum class passed to readEnum: " + enumClass.getName());
        if (nbt.contains(key)) {
            String name = nbt.getStringOr(key, "");
            for (T t : enumConstants) {
                if (t.name().equals(name))
                    return t;
            }
        }
        return enumConstants[0];
    }

    public static <T extends Enum<?>> void writeEnum(CompoundTag nbt, String key, T enumConstant) {
        nbt.putString(key, enumConstant.name());
    }

    public static <T> ListTag writeCompoundList(Iterable<T> list, Function<T, @Nullable CompoundTag> serializer) {
        ListTag listNBT = new ListTag();
        list.forEach(t -> {
            CompoundTag apply = serializer.apply(t);
            if (apply == null)
                return;
            listNBT.add(apply);
        });
        return listNBT;
    }

    public static <T> List<T> readCompoundList(ListTag listNBT, Function<CompoundTag, T> deserializer) {
        List<T> list = new ArrayList<>(listNBT.size());
        listNBT.forEach(inbt -> list.add(deserializer.apply((CompoundTag) inbt)));
        return list;
    }

    public static void iterateCompoundList(ListTag listNBT, Consumer<CompoundTag> consumer) {
        listNBT.forEach(inbt -> consumer.accept((CompoundTag) inbt));
    }

    public static ListTag writeAABB(AABB bb) {
        ListTag bbtag = new ListTag();
        bbtag.add(FloatTag.valueOf((float) bb.minX));
        bbtag.add(FloatTag.valueOf((float) bb.minY));
        bbtag.add(FloatTag.valueOf((float) bb.minZ));
        bbtag.add(FloatTag.valueOf((float) bb.maxX));
        bbtag.add(FloatTag.valueOf((float) bb.maxY));
        bbtag.add(FloatTag.valueOf((float) bb.maxZ));
        return bbtag;
    }

    @Nullable
    public static AABB readAABB(ListTag bbTag) {
        if (bbTag.isEmpty())
            return null;
        return new AABB(
            bbTag.getFloatOr(0, 0),
            bbTag.getFloatOr(1, 0),
            bbTag.getFloatOr(2, 0),
            bbTag.getFloatOr(3, 0),
            bbTag.getFloatOr(4, 0),
            bbTag.getFloatOr(5, 0)
        );
    }

    public static ListTag writeVec3i(Vec3i vec) {
        ListTag tag = new ListTag();
        tag.add(IntTag.valueOf(vec.getX()));
        tag.add(IntTag.valueOf(vec.getY()));
        tag.add(IntTag.valueOf(vec.getZ()));
        return tag;
    }

    public static Vec3i readVec3i(ListTag tag) {
        return new Vec3i(tag.getIntOr(0, 0), tag.getIntOr(1, 0), tag.getIntOr(2, 0));
    }

    public static Tag getINBT(CompoundTag nbt, String id) {
        Tag inbt = nbt.get(id);
        if (inbt != null)
            return inbt;
        return new CompoundTag();
    }

    public static CompoundTag intToCompound(int i) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("V", i);
        return compoundTag;
    }

    public static int intFromCompound(CompoundTag compoundTag) {
        return compoundTag.getIntOr("V", 0);
    }

    public static void writeIdentifier(CompoundTag nbt, String key, Identifier location) {
        nbt.putString(key, location.toString());
    }

    public static Identifier readIdentifier(CompoundTag nbt, String key) {
        return Identifier.parse(nbt.getStringOr(key, ""));
    }

}