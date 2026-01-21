package com.zurrtum.create.client.api.goggles;

import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Implement this interface on the {@link BlockEntity} that wants to add info to the goggle overlay
 */
public non-sealed interface IHaveGoggleInformation extends IHaveCustomOverlayIcon {
    /**
     * This method will be called when looking at a {@link BlockEntity} that implements this interface
     *
     * @return {@code true} if the tooltip creation was successful and should be
     * displayed, or {@code false} if the overlay should not be displayed
     */
    default boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return false;
    }

    default boolean containedFluidTooltip(List<Component> tooltip, boolean isPlayerSneaking, @Nullable FluidInventory handler) {
        if (handler == null)
            return false;

        int size = handler.size();
        if (size == 0)
            return false;

        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);

        boolean isEmpty = true;
        for (int i = 0; i < size; i++) {
            FluidStack fluidStack = handler.getStack(i);
            if (fluidStack.isEmpty())
                continue;

            CreateLang.fluidName(fluidStack).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);

            CreateLang.builder().add(CreateLang.number((double) fluidStack.getAmount() / 81).add(mb).style(ChatFormatting.GOLD))
                .text(ChatFormatting.GRAY, " / ")
                .add(CreateLang.number((double) handler.getMaxAmount(fluidStack) / 81).add(mb).style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

            isEmpty = false;
        }

        if (size > 1) {
            if (isEmpty)
                tooltip.removeLast();
            return true;
        }

        if (!isEmpty)
            return true;

        CreateLang.translate("gui.goggles.fluid_container.capacity")
            .add(CreateLang.number((double) handler.getMaxAmountPerStack() / 81).add(mb).style(ChatFormatting.GOLD)).style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        return true;
    }

}