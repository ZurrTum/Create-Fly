package com.zurrtum.create;

import com.zurrtum.create.api.contraption.ContraptionMovementSetting;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.function.Supplier;

public class AllContraptionMovementSettings {
    public static void register(Block block, Supplier<ContraptionMovementSetting> supplier) {
        ContraptionMovementSetting.REGISTRY.register(block, supplier);
    }

    public static void register() {
        register(Blocks.SPAWNER, () -> AllConfigs.server().kinetics.spawnerMovement.get());
        register(Blocks.BUDDING_AMETHYST, () -> AllConfigs.server().kinetics.amethystMovement.get());
        register(Blocks.OBSIDIAN, () -> AllConfigs.server().kinetics.obsidianMovement.get());
        register(Blocks.CRYING_OBSIDIAN, () -> AllConfigs.server().kinetics.obsidianMovement.get());
        register(Blocks.RESPAWN_ANCHOR, () -> AllConfigs.server().kinetics.obsidianMovement.get());
        register(Blocks.REINFORCED_DEEPSLATE, () -> AllConfigs.server().kinetics.reinforcedDeepslateMovement.get());
    }
}