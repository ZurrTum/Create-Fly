package com.zurrtum.create.client.content.trains.schedule;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.content.trains.schedule.ScheduleDataEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IScheduleInput<T extends ScheduleDataEntry> {
    Pair<ItemStack, Component> getSummary(T input);

    default int slotsTargeted() {
        return 0;
    }

    default List<Component> getTitleAs(T input, String type) {
        ResourceLocation id = input.getId();
        return ImmutableList.of(Component.translatable(id.getNamespace() + ".schedule." + type + "." + id.getPath()));
    }

    default ItemStack getSecondLineIcon() {
        return ItemStack.EMPTY;
    }

    default void setItem(T input, int slot, ItemStack stack) {
    }

    default ItemStack getItem(T input, int slot) {
        return ItemStack.EMPTY;
    }

    @Nullable
    default List<Component> getSecondLineTooltip(int slot) {
        return null;
    }

    default void initConfigurationWidgets(T input, ModularGuiLineBuilder builder) {
    }

    default boolean renderSpecialIcon(T input, GuiGraphics graphics, int x, int y) {
        return false;
    }
}
