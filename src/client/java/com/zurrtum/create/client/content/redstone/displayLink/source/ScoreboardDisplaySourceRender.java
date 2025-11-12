package com.zurrtum.create.client.content.redstone.displayLink.source;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.ChatFormatting;

public class ScoreboardDisplaySourceRender extends ValueListDisplaySourceRender {
    @Override
    public void initConfigurationWidgets(DisplaySource source, DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine)
            builder.addTextInput(
                0, 137, (e, t) -> {
                    e.setValue("");
                    t.withTooltip(ImmutableList.of(
                        CreateLang.translateDirect("display_source.scoreboard.objective").withColor(0x5391E1),
                        CreateLang.translateDirect("gui.schedule.lmb_edit").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                    ));
                }, "Objective"
            );
        else
            addFullNumberConfig(builder);
    }
}
