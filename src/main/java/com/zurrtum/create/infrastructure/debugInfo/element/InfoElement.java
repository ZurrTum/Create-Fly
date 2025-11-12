package com.zurrtum.create.infrastructure.debugInfo.element;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import net.minecraft.world.entity.player.Player;

public sealed interface InfoElement permits DebugInfoSection, InfoEntry {
    void print(int depth, @Nullable Player player, Consumer<String> lineConsumer);

    default void print(@Nullable Player player, Consumer<String> lineConsumer) {
        print(0, player, lineConsumer);
    }
}
