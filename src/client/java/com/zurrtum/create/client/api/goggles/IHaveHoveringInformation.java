package com.zurrtum.create.client.api.goggles;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Implement this interface on the {@link BlockEntity} that wants to add info to the hovering overlay
 */
public non-sealed interface IHaveHoveringInformation extends IHaveCustomOverlayIcon {
    /**
     * This method will be called when looking at a {@link BlockEntity} that implements this interface
     *
     * @return {@code true} if the tooltip creation was successful and should be
     * displayed, or {@code false} if the overlay should not be displayed
     */
    default boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        return false;
    }
}
