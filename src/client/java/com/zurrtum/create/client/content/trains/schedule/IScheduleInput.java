package com.zurrtum.create.client.content.trains.schedule;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.gui.ScreenWithStencils;
import com.zurrtum.create.content.trains.schedule.ScheduleDataEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IScheduleInput<T extends ScheduleDataEntry> {

    Pair<ItemStack, Text> getSummary(T input);

    default int slotsTargeted() {
        return 0;
    }

    default List<Text> getTitleAs(T input, String type) {
        Identifier id = input.getId();
        return ImmutableList.of(Text.translatable(id.getNamespace() + ".schedule." + type + "." + id.getPath()));
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
    default List<Text> getSecondLineTooltip(int slot) {
        return null;
    }

    default void initConfigurationWidgets(T input, ModularGuiLineBuilder builder) {
    }

    default boolean renderSpecialIcon(T input, DrawContext graphics, int x, int y) {
        return false;
    }

    default boolean renderStencilSpecialIcon(
        T input,
        DrawContext graphics,
        VertexConsumerProvider.Immediate vertexConsumers,
        ScreenWithStencils screen,
        int x,
        int y
    ) {
        return false;
    }

}
