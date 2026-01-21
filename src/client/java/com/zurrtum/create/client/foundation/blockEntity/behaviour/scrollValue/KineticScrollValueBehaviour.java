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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class KineticScrollValueBehaviour extends ScrollValueBehaviour<SmartBlockEntity, ServerScrollValueBehaviour> {
    public KineticScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
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
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        ImmutableList<Component> rows = ImmutableList.of(
            Component.literal("⟳").withStyle(ChatFormatting.BOLD),
            Component.literal("⟲").withStyle(ChatFormatting.BOLD)
        );
        ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
        return new ValueSettingsBoard(label, 256, 32, rows, formatter);
    }

    public MutableComponent formatSettings(ValueSettings settings) {
        return CreateLang.number(Math.max(1, Math.abs(settings.value())))
            .add(CreateLang.text(settings.row() == 0 ? "⟳" : "⟲").style(ChatFormatting.BOLD)).component();
    }
}
