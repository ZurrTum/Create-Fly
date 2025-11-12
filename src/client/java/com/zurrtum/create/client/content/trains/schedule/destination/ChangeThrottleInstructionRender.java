package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.destination.ChangeThrottleInstruction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ChangeThrottleInstructionRender implements IScheduleInput<ChangeThrottleInstruction> {
    @Override
    public Pair<ItemStack, Component> getSummary(ChangeThrottleInstruction input) {
        return Pair.of(icon(), formatted(input));
    }

    private MutableComponent formatted(ChangeThrottleInstruction input) {
        return Component.literal(input.intData("Value") + "%");
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return icon();
    }

    @Override
    public List<Component> getTitleAs(ChangeThrottleInstruction input, String type) {
        return ImmutableList.of(CreateLang.translateDirect(
            "schedule." + type + "." + input.getId().getPath() + ".summary",
            formatted(input).withStyle(ChatFormatting.WHITE)
        ).withStyle(ChatFormatting.GOLD));
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
        return AllItems.TRAIN_CONTROLS.getDefaultInstance();
    }

    @Override
    public List<Component> getSecondLineTooltip(int slot) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.instruction.throttle_edit_box"),
            CreateLang.translateDirect("schedule.instruction.throttle_edit_box_1").withStyle(ChatFormatting.GRAY)
        );
    }
}
