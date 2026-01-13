package com.zurrtum.create.client;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.api.behaviour.EntityBehaviour;
import com.zurrtum.create.client.foundation.entity.behaviour.CarriageAudioBehaviour;
import com.zurrtum.create.client.foundation.entity.behaviour.CarriageParticleBehaviour;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.function.Function;

public class AllEntityBehaviours {
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T extends Entity> void add(EntityType<T> type, Function<T, EntityBehaviour<?>>... factories) {
        for (Function<T, EntityBehaviour<?>> factory : factories) {
            EntityBehaviour.CLIENT_REGISTRY.add(type, (Function<Entity, EntityBehaviour<?>>) factory);
        }
    }

    public static void register() {
        add(AllEntityTypes.CARRIAGE_CONTRAPTION, CarriageAudioBehaviour::new, CarriageParticleBehaviour::new);
    }
}
