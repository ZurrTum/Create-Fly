package com.zurrtum.create.client.content.trains.schedule.condition;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.condition.TimedWaitCondition;
import com.zurrtum.create.content.trains.schedule.condition.TimedWaitCondition.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

public abstract class TimedWaitConditionRender<T extends TimedWaitCondition> implements IScheduleInput<T> {
    protected Component formatTime(T input, boolean compact) {
        int value = input.getValue();
        TimeUnit unit = input.getUnit();
        if (compact) {
            return Component.literal(value + unit.suffix);
        }
        return Component.literal(value + " ").append(CreateLang.translateDirect(getUnitKey(unit)));
    }

    @Override
    public List<Component> getTitleAs(T input, String type) {
        ResourceLocation id = input.getId();
        return ImmutableList.of(
            Component.translatable(id.getNamespace() + ".schedule." + type + "." + id.getPath()),
            CreateLang.translateDirect("schedule.condition.for_x_time", formatTime(input, false)).withStyle(ChatFormatting.DARK_AQUA)
        );
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return new ItemStack(Items.REPEATER);
    }

    @Override
    public List<Component> getSecondLineTooltip(int slot) {
        return ImmutableList.of(CreateLang.translateDirect("generic.duration"));
    }

    public String getUnitKey(TimeUnit unit) {
        return "generic.unit." + unit.name().toLowerCase(Locale.ROOT);
    }

    public List<Component> getUnitOptions() {
        return CreateLang.translatedOptions(null, getUnitKey(TimeUnit.TICKS), getUnitKey(TimeUnit.SECONDS), getUnitKey(TimeUnit.MINUTES));
    }

    public void initConfigurationWidgets(T input, ModularGuiLineBuilder builder) {
        builder.addScrollInput(
            0, 31, (i, l) -> {
                i.titled(CreateLang.translateDirect("generic.duration")).withShiftStep(15).withRange(0, 121);
                i.lockedTooltipX = -15;
                i.lockedTooltipY = 35;
            }, "Value"
        );

        builder.addSelectionScrollInput(
            36, 85, (i, l) -> {
                i.forOptions(getUnitOptions()).titled(CreateLang.translateDirect("generic.timeUnit"));
            }, "TimeUnit"
        );
    }
}
