package com.zurrtum.create.client.content.trains.schedule.condition;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.condition.TimedWaitCondition;
import com.zurrtum.create.content.trains.schedule.condition.TimedWaitCondition.TimeUnit;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Locale;

public abstract class TimedWaitConditionRender<T extends TimedWaitCondition> implements IScheduleInput<T> {
    protected Text formatTime(T input, boolean compact) {
        int value = input.getValue();
        TimeUnit unit = input.getUnit();
        if (compact) {
            return Text.literal(value + unit.suffix);
        }
        return Text.literal(value + " ").append(CreateLang.translateDirect(getUnitKey(unit)));
    }

    @Override
    public List<Text> getTitleAs(T input, String type) {
        Identifier id = input.getId();
        return ImmutableList.of(
            Text.translatable(id.getNamespace() + ".schedule." + type + "." + id.getPath()),
            CreateLang.translateDirect("schedule.condition.for_x_time", formatTime(input, false)).formatted(Formatting.DARK_AQUA)
        );
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return new ItemStack(Items.REPEATER);
    }

    @Override
    public List<Text> getSecondLineTooltip(int slot) {
        return ImmutableList.of(CreateLang.translateDirect("generic.duration"));
    }

    public String getUnitKey(TimeUnit unit) {
        return "generic.unit." + unit.name().toLowerCase(Locale.ROOT);
    }

    public List<Text> getUnitOptions() {
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
