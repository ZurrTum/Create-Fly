package com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class ServerBrassDiodeScrollValueBehaviour extends ServerScrollValueBehaviour {
    private static final int TICK = 20;
    private static final int MAX_COUNT = 60;
    private static final int MINUTE = MAX_COUNT * TICK;

    public ServerBrassDiodeScrollValueBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public void onShortInteract(PlayerEntity player, Hand hand, Direction side, BlockHitResult hitResult) {
        if (getWorld().isClient())
            return;
        BlockState blockState = blockEntity.getCachedState();
        if (blockState.getBlock() instanceof BrassDiodeBlock bdb)
            bdb.toggle(getWorld(), getPos(), blockState, player, hand);
    }

    @Override
    public void setValueSettings(PlayerEntity player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = valueSetting.value();
        int multiplier = switch (valueSetting.row()) {
            case 0 -> 1;
            case 1 -> 20;
            default -> 60 * 20;
        };
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(Math.max(2, Math.max(1, value) * multiplier));
    }

    @Override
    public ValueSettings getValueSettings() {
        int row = 0;
        int value = this.value;

        if (value > 60 * 20) {
            value = value / (60 * 20);
            row = 2;
        } else if (value > 60) {
            value = value / 20;
            row = 1;
        }

        return new ValueSettings(row, value);
    }

    @Override
    public String getClipboardKey() {
        return "Timings";
    }
}
