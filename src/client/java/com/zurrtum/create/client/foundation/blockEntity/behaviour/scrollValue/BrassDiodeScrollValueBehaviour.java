package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.client.content.redstone.diodes.BrassDiodeScrollSlot;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class BrassDiodeScrollValueBehaviour extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {
    public BrassDiodeScrollValueBehaviour(SmartBlockEntity be) {
        super(CreateLang.translateDirect("logistics.redstone_interval"), be, new BrassDiodeScrollSlot());
        withFormatter(BrassDiodeScrollValueBehaviour::format);
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
            label,
            60,
            10,
            CreateLang.translatedOptions("generic.unit", "ticks", "seconds", "minutes"),
            new ValueSettingsFormatter(this::formatSettings)
        );
    }

    public MutableComponent formatSettings(ValueSettings settings) {
        int value = Math.max(1, settings.value());
        return Component.literal(switch (settings.row()) {
            case 0 -> Math.max(2, value) + "t";
            case 1 -> "0:" + (value < 10 ? "0" : "") + value;
            default -> value + ":00";
        });
    }

    private static String format(int value) {
        if (value < 60)
            return value + "t";
        if (value < 20 * 60)
            return (value / 20) + "s";
        return (value / 20 / 60) + "m";
    }
}
