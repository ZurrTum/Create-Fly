package com.zurrtum.create.infrastructure.debugInfo.element;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A supplier of debug information. May be queried on the client or server.
 */
@FunctionalInterface
public interface InfoProvider {
    /**
     * @param player the player requesting the data. May be null
     */
    @Nullable String getInfo(@Nullable PlayerEntity player);

    default String getInfoSafe(PlayerEntity player) {
        try {
            return Objects.toString(getInfo(player));
        } catch (Throwable t) {
            StringBuilder builder = new StringBuilder("Error getting information!");
            builder.append(' ').append(t.getMessage());
            for (StackTraceElement element : t.getStackTrace()) {
                builder.append('\n').append("\t").append(element.toString());
            }
            return builder.toString();
        }
    }
}
