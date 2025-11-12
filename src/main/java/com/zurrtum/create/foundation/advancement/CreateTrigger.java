package com.zurrtum.create.foundation.advancement;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

public class CreateTrigger implements CriterionTrigger<CreateTrigger.Conditions> {
    public final Map<PlayerAdvancements, Set<Listener<Conditions>>> listeners = Maps.newHashMap();
    public final ResourceLocation id;

    public CreateTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements playerAdvancementsIn, Listener<Conditions> listener) {
        listeners.computeIfAbsent(playerAdvancementsIn, k -> new HashSet<>()).add(listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements playerAdvancementsIn, Listener<Conditions> listener) {
        Set<Listener<Conditions>> playerListeners = this.listeners.get(playerAdvancementsIn);
        if (playerListeners != null) {
            playerListeners.remove(listener);
            if (playerListeners.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player) {
        PlayerAdvancements playerAdvancements = player.getAdvancements();
        Set<Listener<Conditions>> playerListeners = this.listeners.get(playerAdvancements);
        if (playerListeners != null) {
            for (Listener<Conditions> listener : playerListeners) {
                listener.run(playerAdvancements);
            }
        }
    }

    public static class Conditions implements CriterionTriggerInstance {
        public static final Codec<Conditions> CODEC = Codec.unit(new Conditions());

        @Override
        public void validate(CriterionValidator validator) {
        }
    }
}