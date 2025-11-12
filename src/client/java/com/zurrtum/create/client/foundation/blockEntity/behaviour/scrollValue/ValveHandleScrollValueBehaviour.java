package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;


import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.content.kinetics.crank.ValveHandleValueBox;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class ValveHandleScrollValueBehaviour extends ScrollValueBehaviour<ValveHandleBlockEntity, ServerScrollValueBehaviour> {

    public ValveHandleScrollValueBehaviour(ValveHandleBlockEntity be) {
        super(CreateLang.translateDirect("kinetics.valve_handle.rotated_angle"), be, new ValveHandleValueBox());
        withFormatter(v -> Math.abs(v) + CreateLang.translateDirect("generic.unit.degrees").getString());
        onlyActiveWhen(be::showValue);
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        ImmutableList<Component> rows = ImmutableList.of(
            Component.literal("\u27f3").withStyle(ChatFormatting.BOLD),
            Component.literal("\u27f2").withStyle(ChatFormatting.BOLD)
        );
        return new ValueSettingsBoard(label, 180, 45, rows, new ValueSettingsFormatter(this::formatValue));
    }

    public MutableComponent formatValue(ValueSettings settings) {
        return CreateLang.number(Math.max(1, Math.abs(settings.value()))).add(CreateLang.translateDirect("generic.unit.degrees")).component();
    }
}