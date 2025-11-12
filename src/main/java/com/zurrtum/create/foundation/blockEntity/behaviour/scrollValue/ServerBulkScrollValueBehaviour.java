package com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.entity.player.Player;

public class ServerBulkScrollValueBehaviour extends ServerScrollValueBehaviour {
    Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter;

    public ServerBulkScrollValueBehaviour(SmartBlockEntity be, Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter) {
        super(be);
        this.groupGetter = groupGetter;
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
        if (!ctrlDown) {
            super.setValueSettings(player, valueSetting, ctrlDown);
            return;
        }
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        for (SmartBlockEntity be : getBulk()) {
            ServerScrollValueBehaviour other = be.getBehaviour(ServerScrollValueBehaviour.TYPE);
            if (other != null)
                other.setValue(valueSetting.value());
        }
    }

    public List<? extends SmartBlockEntity> getBulk() {
        return groupGetter.apply(blockEntity);
    }
}
