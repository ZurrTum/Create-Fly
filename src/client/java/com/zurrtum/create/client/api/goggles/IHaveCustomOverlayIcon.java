package com.zurrtum.create.client.api.goggles;

import com.zurrtum.create.AllItems;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;

public sealed interface IHaveCustomOverlayIcon permits IHaveGoggleInformation, IHaveHoveringInformation {
    /**
     * This method will be called when looking at a {@link BlockEntity} that implements {@link IHaveGoggleInformation}
     * or {@link IHaveHoveringInformation}
     *
     * @return The {@link ItemStack} you want the overlay to show instead of the goggles
     */
    default ItemStack getIcon(boolean isPlayerSneaking) {
        return new ItemStack(AllItems.GOGGLES);
    }
}
