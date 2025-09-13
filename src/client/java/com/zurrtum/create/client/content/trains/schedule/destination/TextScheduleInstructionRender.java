package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.destination.TextScheduleInstruction;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public abstract class TextScheduleInstructionRender<T extends TextScheduleInstruction> implements IScheduleInput<T> {
    @Override
    public List<Text> getTitleAs(T input, String type) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule." + type + "." + input.getId().getPath() + ".summary").formatted(Formatting.GOLD),
            CreateLang.translateDirect("generic.in_quotes", Text.literal(input.getLabelText()))
        );
    }

    public void initConfigurationWidgets(T input, ModularGuiLineBuilder builder) {
        builder.addTextInput(0, 121, (e, t) -> modifyEditBox(e), "Text");
    }

    protected void modifyEditBox(TextFieldWidget box) {
    }
}
