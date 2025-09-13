package com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.function.Function;

public class ServerChassisScrollValueBehaviour extends ServerBulkScrollValueBehaviour {
    public ServerChassisScrollValueBehaviour(SmartBlockEntity be, Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter) {
        super(be, groupGetter);
    }

    @Override
    public void setValueSettings(PlayerEntity player, ValueSettings vs, boolean ctrlHeld) {
        super.setValueSettings(player, new ValueSettings(vs.row(), vs.value() + 1), ctrlHeld);
    }

    @Override
    public ValueSettings getValueSettings() {
        ValueSettings vs = super.getValueSettings();
        return new ValueSettings(vs.row(), vs.value() - 1);
    }

    @Override
    public String getClipboardKey() {
        return "Chassis";
    }
}
