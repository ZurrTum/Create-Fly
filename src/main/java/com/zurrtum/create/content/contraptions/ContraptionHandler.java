package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.catnip.data.WorldAttached;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;

import java.lang.ref.WeakReference;
import java.util.*;

public class ContraptionHandler {

    /* Global map of loaded contraptions */

    public static WorldAttached<Map<Integer, WeakReference<AbstractContraptionEntity>>> loadedContraptions;
    static WorldAttached<List<AbstractContraptionEntity>> queuedAdditions;

    static {
        loadedContraptions = new WorldAttached<>($ -> new HashMap<>());
        queuedAdditions = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
    }

    public static void tick(World world) {
        Map<Integer, WeakReference<AbstractContraptionEntity>> map = loadedContraptions.get(world);
        List<AbstractContraptionEntity> queued = queuedAdditions.get(world);

        for (AbstractContraptionEntity contraptionEntity : queued)
            map.put(contraptionEntity.getId(), new WeakReference<>(contraptionEntity));
        queued.clear();

        Collection<WeakReference<AbstractContraptionEntity>> values = map.values();
        for (Iterator<WeakReference<AbstractContraptionEntity>> iterator = values.iterator(); iterator.hasNext(); ) {
            WeakReference<AbstractContraptionEntity> weakReference = iterator.next();
            AbstractContraptionEntity contraptionEntity = weakReference.get();
            if (contraptionEntity == null || !contraptionEntity.isAliveOrStale()) {
                iterator.remove();
                continue;
            }
            if (!contraptionEntity.isAlive()) {
                contraptionEntity.staleTicks--;
                continue;
            }

            ContraptionCollider.collideEntities(contraptionEntity);
        }
    }

    public static void addSpawnedContraptionsToCollisionList(EntityLike entity) {
        if (entity instanceof AbstractContraptionEntity abstractContraptionEntity)
            queuedAdditions.get(abstractContraptionEntity.getEntityWorld()).add(abstractContraptionEntity);
    }

}
