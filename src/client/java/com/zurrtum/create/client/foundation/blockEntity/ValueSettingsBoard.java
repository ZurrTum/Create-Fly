package com.zurrtum.create.client.foundation.blockEntity;

import net.minecraft.network.chat.Component;

import java.util.List;

public record ValueSettingsBoard(Component title, int maxValue, int milestoneInterval, List<Component> rows,
                                 ValueSettingsFormatter formatter) {
}