package com.zurrtum.create.content.schematics;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Iterator;

public class SchematicAndQuillItem extends Item {

    public SchematicAndQuillItem(Settings properties) {
        super(properties);
    }

    public static void replaceStructureVoidWithAir(NbtCompound nbt) {
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

    public static void clampGlueBoxes(World level, Box aabb, NbtCompound nbt) {
        NbtList listtag = nbt.getListOrEmpty("entities").copy();

        for (Iterator<NbtElement> iterator = listtag.iterator(); iterator.hasNext(); ) {
            NbtElement tag = iterator.next();
            if (!(tag instanceof NbtCompound compoundtag))
                continue;
            if (compoundtag.getCompound("nbt").flatMap(compound -> compound.get("id", Identifier.CODEC))
                .map(id -> id.equals(EntityType.getId(AllEntityTypes.SUPER_GLUE))).orElse(false)) {
                iterator.remove();
            }
        }

        for (SuperGlueEntity entity : SuperGlueEntity.collectCropped(level, aabb)) {
            Vec3d vec3 = new Vec3d(entity.getX() - aabb.minX, entity.getY() - aabb.minY, entity.getZ() - aabb.minZ);
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getErrorReporterContext(), Create.LOGGER)) {
                NbtWriteView view = NbtWriteView.create(logging, entity.getEntityWorld().getRegistryManager());
                entity.saveData(view);
                BlockPos blockpos = BlockPos.ofFloored(vec3);

                NbtCompound entityTag = new NbtCompound();
                entityTag.put("pos", Vec3d.CODEC, vec3);
                entityTag.put("blockPos", BlockPos.CODEC, blockpos);
                entityTag.put("nbt", view.getNbt());
                listtag.add(entityTag);
            }
        }

        nbt.put("entities", listtag);
    }
}
