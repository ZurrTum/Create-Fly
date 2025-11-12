package com.zurrtum.create.content.schematics;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SchematicAndQuillItem extends Item {

    public SchematicAndQuillItem(Properties properties) {
        super(properties);
    }

    public static void replaceStructureVoidWithAir(CompoundTag nbt) {
        String air = RegisteredObjectsHelper.getKeyOrThrow(Blocks.AIR).toString();
        String structureVoid = RegisteredObjectsHelper.getKeyOrThrow(Blocks.STRUCTURE_VOID).toString();

        NBTHelper.iterateCompoundList(
            nbt.getListOrEmpty("palette"), c -> {
                if (c.getString("Name").map(name -> name.equals(structureVoid)).orElse(false)) {
                    c.putString("Name", air);
                }
            }
        );
    }

    public static void clampGlueBoxes(Level level, AABB aabb, CompoundTag nbt) {
        ListTag listtag = nbt.getListOrEmpty("entities").copy();

        for (Iterator<Tag> iterator = listtag.iterator(); iterator.hasNext(); ) {
            Tag tag = iterator.next();
            if (!(tag instanceof CompoundTag compoundtag))
                continue;
            if (compoundtag.getCompound("nbt").flatMap(compound -> compound.read("id", ResourceLocation.CODEC))
                .map(id -> id.equals(EntityType.getKey(AllEntityTypes.SUPER_GLUE))).orElse(false)) {
                iterator.remove();
            }
        }

        for (SuperGlueEntity entity : SuperGlueEntity.collectCropped(level, aabb)) {
            Vec3 vec3 = new Vec3(entity.getX() - aabb.minX, entity.getY() - aabb.minY, entity.getZ() - aabb.minZ);
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(entity.problemPath(), Create.LOGGER)) {
                TagValueOutput view = TagValueOutput.createWithContext(logging, entity.level().registryAccess());
                entity.save(view);
                BlockPos blockpos = BlockPos.containing(vec3);

                CompoundTag entityTag = new CompoundTag();
                entityTag.store("pos", Vec3.CODEC, vec3);
                entityTag.store("blockPos", BlockPos.CODEC, blockpos);
                entityTag.put("nbt", view.buildResult());
                listtag.add(entityTag);
            }
        }

        nbt.put("entities", listtag);
    }
}
