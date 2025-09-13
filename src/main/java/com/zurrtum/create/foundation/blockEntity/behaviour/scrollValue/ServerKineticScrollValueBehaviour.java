package com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.entity.player.PlayerEntity;

public class ServerKineticScrollValueBehaviour extends ServerScrollValueBehaviour {
    public ServerKineticScrollValueBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public void setValueSettings(PlayerEntity player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = Math.max(1, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(valueSetting.row() == 0 ? -value : value);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(value < 0 ? 0 : 1, Math.abs(value));
    }

    @Override
    public String getClipboardKey() {
        return "Speed";
    }
}
