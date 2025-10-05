package com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.content.kinetics.crank.ValveHandleBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class ServerValveScrollValueBehaviour extends ServerScrollValueBehaviour {
    public ServerValveScrollValueBehaviour(SmartBlockEntity be) {
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
    public void onShortInteract(PlayerEntity player, Hand hand, Direction side, BlockHitResult hitResult) {
        if (getWorld().isClient())
            return;
        BlockState blockState = blockEntity.getCachedState();
        if (blockState.getBlock() instanceof ValveHandleBlock vhb)
            vhb.clicked(getWorld(), getPos(), blockState, player, hand);
    }
}
