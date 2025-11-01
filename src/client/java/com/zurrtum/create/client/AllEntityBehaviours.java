package com.zurrtum.create.client;

import com.zurrtum.create.client.foundation.entity.behaviour.CarriageAudioBehaviour;
import com.zurrtum.create.client.foundation.entity.behaviour.CarriageParticleBehaviour;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.foundation.entity.behaviour.EntityBehaviour;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public class AllEntityBehaviours {
    public static Map<Class<? extends Entity>, Function<? extends Entity, EntityBehaviour<?>>[]> ALL = new Reference2ObjectArrayMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Entity> void addBehaviours(T blockEntity, ArrayList<EntityBehaviour<?>> behaviours) {
        Function<? extends Entity, EntityBehaviour<?>>[] factorys = ALL.get(blockEntity.getClass());
        if (factorys != null) {
            for (Function<T, EntityBehaviour<?>> factory : (Function<T, EntityBehaviour<?>>[]) factorys) {
                behaviours.add(factory.apply(blockEntity));
            }
        }
    }

    @SafeVarargs
    public static <T extends Entity> void add(Class<T> type, Function<T, EntityBehaviour<?>>... factory) {
        ALL.put(type, factory);
    }

    public static void register() {
        add(CarriageContraptionEntity.class, CarriageAudioBehaviour::new, CarriageParticleBehaviour::new);
    }
}
