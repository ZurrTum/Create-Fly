package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.content.kinetics.motor.MotorValueBox;
import com.zurrtum.create.client.content.kinetics.speedController.ControllerValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;

public class KineticScrollValueBehaviour extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {
    public KineticScrollValueBehaviour(Text label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
        withFormatter(v -> String.valueOf(Math.abs(v)));
    }

    public static KineticScrollValueBehaviour motor(CreativeMotorBlockEntity blockEntity) {
        return new KineticScrollValueBehaviour(
            CreateLang.translateDirect("kinetics.creative_motor.rotation_speed"),
            blockEntity,
            new MotorValueBox()
        );
    }

    public static KineticScrollValueBehaviour controller(SpeedControllerBlockEntity blockEntity) {
        return new KineticScrollValueBehaviour(
            CreateLang.translateDirect("kinetics.speed_controller.rotation_speed"),
            blockEntity,
            new ControllerValueBoxTransform()
        );
    }

    @Override
    public ValueSettingsBoard createBoard(PlayerEntity player, BlockHitResult hitResult) {
        ImmutableList<Text> rows = ImmutableList.of(
            Text.literal("\u27f3").formatted(Formatting.BOLD),
            Text.literal("\u27f2").formatted(Formatting.BOLD)
        );
        ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
        return new ValueSettingsBoard(label, 256, 32, rows, formatter);
    }

    public MutableText formatSettings(ValueSettings settings) {
        return CreateLang.number(Math.max(1, Math.abs(settings.value())))
            .add(CreateLang.text(settings.row() == 0 ? "\u27f3" : "\u27f2").style(Formatting.BOLD)).component();
    }
}
