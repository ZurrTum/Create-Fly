package com.zurrtum.create.api.entity;

import com.zurrtum.create.infrastructure.player.FakePlayerEntity;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;

public interface FakePlayerHandler {
    boolean FABRIC = FabricLoader.getInstance().isModLoaded("fabric-events-interaction-v0");

    static boolean has(Entity player) {
        if (FABRIC && player instanceof FakePlayer) {
            return true;
        }
        return player instanceof FakePlayerEntity;
    }
}
