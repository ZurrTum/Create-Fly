package com.zurrtum.create.foundation.advancement;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CreateTrigger implements Criterion<CreateTrigger.Conditions> {
    public final Map<PlayerAdvancementTracker, Set<ConditionsContainer<Conditions>>> listeners = Maps.newHashMap();
    public final Identifier id;

    public CreateTrigger(Identifier id) {
        this.id = id;
    }

    @Override
    public void beginTrackingCondition(PlayerAdvancementTracker playerAdvancementsIn, ConditionsContainer<Conditions> listener) {
        listeners.computeIfAbsent(playerAdvancementsIn, k -> new HashSet<>()).add(listener);
    }

    @Override
    public void endTrackingCondition(PlayerAdvancementTracker playerAdvancementsIn, ConditionsContainer<Conditions> listener) {
        Set<ConditionsContainer<Conditions>> playerListeners = this.listeners.get(playerAdvancementsIn);
        if (playerListeners != null) {
            playerListeners.remove(listener);
            if (playerListeners.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void endTracking(PlayerAdvancementTracker playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        PlayerAdvancementTracker playerAdvancements = player.getAdvancementTracker();
        Set<ConditionsContainer<Conditions>> playerListeners = this.listeners.get(playerAdvancements);
        if (playerListeners != null) {
            for (ConditionsContainer<Conditions> listener : playerListeners) {
                listener.grant(playerAdvancements);
            }
        }
    }

    public static class Conditions implements CriterionConditions {
        public static final Codec<Conditions> CODEC = Codec.unit(new Conditions());

        @Override
        public void validate(LootContextPredicateValidator validator) {
        }
    }
}