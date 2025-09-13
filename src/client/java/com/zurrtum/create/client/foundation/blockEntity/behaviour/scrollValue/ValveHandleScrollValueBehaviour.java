package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;


import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.content.kinetics.crank.ValveHandleValueBox;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;

public class ValveHandleScrollValueBehaviour extends ScrollValueBehaviour<ValveHandleBlockEntity, ServerScrollValueBehaviour> {

    public ValveHandleScrollValueBehaviour(ValveHandleBlockEntity be) {
        super(CreateLang.translateDirect("kinetics.valve_handle.rotated_angle"), be, new ValveHandleValueBox());
        withFormatter(v -> Math.abs(v) + CreateLang.translateDirect("generic.unit.degrees").getString());
        onlyActiveWhen(be::showValue);
    }

    @Override
    public ValueSettingsBoard createBoard(PlayerEntity player, BlockHitResult hitResult) {
        ImmutableList<Text> rows = ImmutableList.of(
            Text.literal("\u27f3").formatted(Formatting.BOLD),
            Text.literal("\u27f2").formatted(Formatting.BOLD)
        );
        return new ValueSettingsBoard(label, 180, 45, rows, new ValueSettingsFormatter(this::formatValue));
    }

    public MutableText formatValue(ValueSettings settings) {
        return CreateLang.number(Math.max(1, Math.abs(settings.value()))).add(CreateLang.translateDirect("generic.unit.degrees")).component();
    }
}