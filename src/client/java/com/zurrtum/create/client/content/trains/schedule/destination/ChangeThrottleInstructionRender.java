package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.destination.ChangeThrottleInstruction;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ChangeThrottleInstructionRender implements IScheduleInput<ChangeThrottleInstruction> {
    @Override
    public Pair<ItemStack, Text> getSummary(ChangeThrottleInstruction input) {
        return Pair.of(icon(), formatted(input));
    }

    private MutableText formatted(ChangeThrottleInstruction input) {
        return Text.literal(input.intData("Value") + "%");
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return icon();
    }

    @Override
    public List<Text> getTitleAs(ChangeThrottleInstruction input, String type) {
        return ImmutableList.of(CreateLang.translateDirect(
            "schedule." + type + "." + input.getId().getPath() + ".summary",
            formatted(input).formatted(Formatting.WHITE)
        ).formatted(Formatting.GOLD));
    }

    public void initConfigurationWidgets(ChangeThrottleInstruction input, ModularGuiLineBuilder builder) {
        builder.addScrollInput(
            0, 50, (si, l) -> {
                si.withRange(5, 101).withStepFunction(c -> c.shift ? 25 : 5)
                    .titled(CreateLang.translateDirect("schedule.instruction.throttle_edit_box"));
                l.withSuffix("%");
            }, "Value"
        );
    }

    private ItemStack icon() {
        return AllItems.TRAIN_CONTROLS.getDefaultStack();
    }

    @Override
    public List<Text> getSecondLineTooltip(int slot) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.instruction.throttle_edit_box"),
            CreateLang.translateDirect("schedule.instruction.throttle_edit_box_1").formatted(Formatting.GRAY)
        );
    }
}
