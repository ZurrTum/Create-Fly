package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.content.equipment.clipboard.ClipboardCloneable;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

public interface ValueSettingsHandleBehaviour extends ClipboardCloneable {
    default boolean acceptsValueSettings() {
        return true;
    }

    default void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
    }

    default boolean mayInteract(Player player) {
        return true;
    }

    default int netId() {
        return 0;
    }

    ValueSettings getValueSettings();

    default void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
    }

    default void playFeedbackSound(BlockEntityBehaviour<?> origin) {
        origin.getLevel().playSound(null, origin.getPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.25f, 2f);
        origin.getLevel().playSound(null, origin.getPos(), SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), SoundSource.BLOCKS, 0.03f, 1.125f);
    }

    @Override
    default String getClipboardKey() {
        return "Settings";
    }

    @Override
    default boolean canWrite(HolderLookup.Provider registries, Direction side) {
        return acceptsValueSettings();
    }

    @Override
    default boolean writeToClipboard(ValueOutput view, Direction side) {
        if (!acceptsValueSettings())
            return false;
        ValueSettings valueSettings = getValueSettings();
        view.putInt("Value", valueSettings.value());
        view.putInt("Row", valueSettings.row());
        return true;
    }

    @Override
    default boolean readFromClipboard(ValueInput view, Player player, Direction side, boolean simulate) {
        if (!acceptsValueSettings())
            return false;
        Optional<Integer> row = view.getInt("Row");
        if (row.isEmpty()) {
            return false;
        }
        Optional<Integer> value = view.getInt("Value");
        if (value.isEmpty()) {
            return false;
        }
        if (simulate)
            return true;
        setValueSettings(player, new ValueSettings(row.get(), value.get()), false);
        return true;
    }
}
