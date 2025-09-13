package com.zurrtum.create.content.schematics;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class SchematicInstances {

    private static final WorldAttached<Cache<Integer, SchematicLevel>> LOADED_SCHEMATICS = new WorldAttached<>($ -> CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES).build());

    @Nullable
    public static SchematicLevel get(World world, ItemStack schematic) {
        Cache<Integer, SchematicLevel> map = LOADED_SCHEMATICS.get(world);
        int hash = getHash(schematic);
        SchematicLevel ifPresent = map.getIfPresent(hash);
        if (ifPresent != null)
            return ifPresent;
        SchematicLevel loadWorld = loadWorld(world, schematic);
        if (loadWorld == null)
            return null;
        map.put(hash, loadWorld);
        return loadWorld;
    }

    private static SchematicLevel loadWorld(World wrapped, ItemStack schematic) {
        if (schematic == null || !schematic.contains(AllDataComponents.SCHEMATIC_FILE))
            return null;
        if (!schematic.contains(AllDataComponents.SCHEMATIC_DEPLOYED))
            return null;

        StructureTemplate activeTemplate = SchematicItem.loadSchematic(wrapped, schematic);

        if (activeTemplate.getSize().equals(Vec3i.ZERO))
            return null;

        BlockPos anchor = schematic.get(AllDataComponents.SCHEMATIC_ANCHOR);
        SchematicLevel world = new SchematicLevel(anchor, wrapped);
        StructurePlacementData settings = SchematicItem.getSettings(schematic);
        activeTemplate.place(world, anchor, anchor, settings, wrapped.getRandom(), Block.NOTIFY_LISTENERS);

        StructureTransform transform = new StructureTransform(settings.getPosition(), Direction.Axis.Y, settings.getRotation(), settings.getMirror());
        for (BlockEntity be : world.getBlockEntities())
            transform.apply(be);

        return world;
    }

    public static void clearHash(ItemStack schematic) {
        if (schematic == null || !schematic.contains(AllDataComponents.SCHEMATIC_FILE))
            return;
        schematic.remove(AllDataComponents.SCHEMATIC_HASH);
    }

    public static int getHash(ItemStack schematic) {
        if (schematic == null || !schematic.contains(AllDataComponents.SCHEMATIC_FILE))
            return -1;
        if (!schematic.contains(AllDataComponents.SCHEMATIC_HASH))
            schematic.set(AllDataComponents.SCHEMATIC_HASH, schematic.getComponentChanges().hashCode());
        return schematic.getOrDefault(AllDataComponents.SCHEMATIC_HASH, -1);
    }

}
