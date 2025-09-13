package com.zurrtum.create.client.content.trains.schedule.condition;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.condition.PlayerPassengerCondition;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class PlayerPassengerConditionRender implements IScheduleInput<PlayerPassengerCondition> {
    @Override
    public Pair<ItemStack, Text> getSummary(PlayerPassengerCondition input) {
        int target = input.getTarget();
        return Pair.of(
            AllItems.YELLOW_SEAT.getDefaultStack(),
            CreateLang.translateDirect("schedule.condition.player_count." + (target == 1 ? "summary" : "summary_plural"), target)
        );
    }

    @Override
    public List<Text> getTitleAs(PlayerPassengerCondition input, String type) {
        int target = input.getTarget();
        return ImmutableList.of(CreateLang.translateDirect(
            "schedule.condition.player_count.seated", CreateLang.translateDirect(
                "schedule.condition.player_count." + (target == 1 ? "summary" : "summary_plural"),
                Text.literal("" + target).formatted(Formatting.DARK_AQUA)
            )
        ));
    }

    @Override
    public void initConfigurationWidgets(PlayerPassengerCondition input, ModularGuiLineBuilder builder) {
        builder.addScrollInput(
            0, 31, (i, l) -> {
                i.titled(CreateLang.translateDirect("schedule.condition.player_count.players")).withShiftStep(5).withRange(0, 21);
            }, "Count"
        );

        builder.addSelectionScrollInput(
            36, 85, (i, l) -> {
                i.forOptions(CreateLang.translatedOptions("schedule.condition.player_count", "exactly", "or_above"))
                    .titled(CreateLang.translateDirect("schedule.condition.player_count.condition"));
            }, "Exact"
        );
    }
}
