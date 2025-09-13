package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.content.contraptions.chassis.ChassisRangeDisplay;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.chassis.ChassisBlockEntity;
import com.zurrtum.create.content.contraptions.chassis.RadialChassisBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerBulkScrollValueBehaviour;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;

public class ChassisScrollValueBehaviour extends ScrollValueBehaviour<ChassisBlockEntity, ServerBulkScrollValueBehaviour> {

    public ChassisScrollValueBehaviour(ChassisBlockEntity be) {
        super(
            CreateLang.translateDirect(be.getCachedState()
                .getBlock() instanceof RadialChassisBlock ? "contraptions.chassis.radius" : "contraptions.chassis.range"),
            be,
            new CenteredSideValueBoxTransform()
        );
        formatter = s -> String.valueOf(blockEntity.currentlySelectedRange);
        needsWrench = true;
    }

    @Override
    public ValueSettingsBoard createBoard(PlayerEntity player, BlockHitResult hitResult) {
        ImmutableList<Text> rows = ImmutableList.of(CreateLang.translateDirect("contraptions.chassis.distance"));
        ValueSettingsFormatter formatter = new ValueSettingsFormatter(vs -> ValueSettingsFormatter.toLocaleNumber(new ValueSettings(
            vs.row(),
            vs.value() + 1
        )));
        return new ValueSettingsBoard(label, behaviour.getMax() - 1, 1, rows, formatter);
    }

    @Override
    public void newSettingHovered(ValueSettings valueSetting) {
        if (!Screen.hasControlDown()) {
            blockEntity.currentlySelectedRange = valueSetting.value() + 1;
        } else
            for (SmartBlockEntity be : behaviour.getBulk())
                if (be instanceof ChassisBlockEntity cbe)
                    cbe.currentlySelectedRange = valueSetting.value() + 1;
        ChassisRangeDisplay.display(blockEntity);
    }
}