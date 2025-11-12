package com.zurrtum.create.client.foundation.blockEntity;

import java.util.List;

import net.minecraft.network.chat.Component;

public record ValueSettingsBoard(
    Component title, int maxValue, int milestoneInterval, List<Component> rows, ValueSettingsFormatter formatter
) {
}