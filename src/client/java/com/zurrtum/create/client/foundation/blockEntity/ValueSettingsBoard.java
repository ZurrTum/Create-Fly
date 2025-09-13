package com.zurrtum.create.client.foundation.blockEntity;

import net.minecraft.text.Text;

import java.util.List;

public record ValueSettingsBoard(
    Text title, int maxValue, int milestoneInterval, List<Text> rows, ValueSettingsFormatter formatter
) {
}