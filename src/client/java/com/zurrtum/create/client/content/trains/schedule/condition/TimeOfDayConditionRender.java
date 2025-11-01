package com.zurrtum.create.client.content.trains.schedule.condition;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.condition.TimeOfDayCondition;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;

public class TimeOfDayConditionRender implements IScheduleInput<TimeOfDayCondition> {
    @Override
    public Pair<ItemStack, Text> getSummary(TimeOfDayCondition input) {
        return Pair.of(new ItemStack(Items.STRUCTURE_VOID), input.getDigitalDisplay(input.intData("Hour"), input.intData("Minute"), false));
    }

    @Override
    public List<Text> getTitleAs(TimeOfDayCondition input, String type) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.condition.time_of_day.scheduled"),
            input.getDigitalDisplay(input.intData("Hour"), input.intData("Minute"), false).formatted(Formatting.DARK_AQUA)
                .append(Text.literal(" -> ").formatted(Formatting.DARK_GRAY)).append(CreateLang.translatedOptions(
                    "schedule.condition.time_of_day.rotation",
                    "every_24",
                    "every_12",
                    "every_6",
                    "every_4",
                    "every_3",
                    "every_2",
                    "every_1",
                    "every_0_45",
                    "every_0_30",
                    "every_0_15"
                ).get(input.intData("Rotation")).copy().formatted(Formatting.GRAY))
        );
    }

    public String twoDigits(int t) {
        return t < 10 ? "0" + t : "" + t;
    }

    private Identifier getClockTextureId(TimeOfDayCondition input) {
        int displayHr = (input.intData("Hour") + 12) % 24;
        float progress = (displayHr * 60f + input.intData("Minute")) / (24 * 60);
        return Identifier.ofVanilla("textures/item/clock_" + twoDigits(MathHelper.clamp((int) (progress * 64), 0, 63)) + ".png");
    }

    @Override
    public boolean renderSpecialIcon(TimeOfDayCondition input, DrawContext graphics, int x, int y) {
        graphics.drawTexture(RenderPipelines.GUI_TEXTURED, getClockTextureId(input), x, y, 0, 0, 16, 16, 16, 16);
        return true;
    }

    @Override
    public void initConfigurationWidgets(TimeOfDayCondition input, ModularGuiLineBuilder builder) {
        MutableObject<ScrollInput> minuteInput = new MutableObject<>();
        MutableObject<ScrollInput> hourInput = new MutableObject<>();
        MutableObject<Label> timeLabel = new MutableObject<>();

        builder.addScrollInput(
            0, 16, (i, l) -> {
                i.withRange(0, 24);
                timeLabel.setValue(l);
                hourInput.setValue(i);
            }, "Hour"
        );

        builder.addScrollInput(
            18, 16, (i, l) -> {
                i.withRange(0, 60);
                minuteInput.setValue(i);
                l.visible = false;
            }, "Minute"
        );

        builder.addSelectionScrollInput(
            52, 62, (i, l) -> {
                i.forOptions(CreateLang.translatedOptions(
                    "schedule.condition.time_of_day.rotation",
                    "every_24",
                    "every_12",
                    "every_6",
                    "every_4",
                    "every_3",
                    "every_2",
                    "every_1",
                    "every_0_45",
                    "every_0_30",
                    "every_0_15"
                )).titled(CreateLang.translateDirect("schedule.condition.time_of_day.rotation"));
            }, "Rotation"
        );

        NbtCompound data = input.getData();
        hourInput.getValue().titled(CreateLang.translateDirect("generic.daytime.hour")).calling(t -> {
            data.putInt("Hour", t);
            timeLabel.getValue().text = input.getDigitalDisplay(t, minuteInput.getValue().getState(), true);
        }).writingTo(null).withShiftStep(6);

        minuteInput.getValue().titled(CreateLang.translateDirect("generic.daytime.minute")).calling(t -> {
            data.putInt("Minute", t);
            timeLabel.getValue().text = input.getDigitalDisplay(hourInput.getValue().getState(), t, true);
        }).writingTo(null).withShiftStep(15);

        minuteInput.getValue().lockedTooltipX = hourInput.getValue().lockedTooltipX = -15;
        minuteInput.getValue().lockedTooltipY = hourInput.getValue().lockedTooltipY = 35;

        hourInput.getValue().setState(input.intData("Hour"));
        minuteInput.getValue().setState(input.intData("Minute")).onChanged();

        builder.customArea(0, 52);
        builder.customArea(52, 69);
    }
}
