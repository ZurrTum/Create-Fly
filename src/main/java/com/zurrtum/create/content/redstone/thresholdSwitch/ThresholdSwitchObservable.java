package com.zurrtum.create.content.redstone.thresholdSwitch;

import net.minecraft.text.MutableText;

public interface ThresholdSwitchObservable {

    int getMaxValue();

    int getMinValue();

    int getCurrentValue();

    MutableText format(int value);

}
