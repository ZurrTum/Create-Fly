package com.zurrtum.create.infrastructure.debugInfo.element;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public sealed interface InfoElement permits DebugInfoSection, InfoEntry {
    void print(int depth, @Nullable PlayerEntity player, Consumer<String> lineConsumer);

    default void print(@Nullable PlayerEntity player, Consumer<String> lineConsumer) {
        print(0, player, lineConsumer);
    }
}
