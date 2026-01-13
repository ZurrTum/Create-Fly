package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.equipment.clipboard.ClipboardCloneable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

import java.util.Optional;

public interface ValueSettingsHandleBehaviour extends ClipboardCloneable {
    default boolean acceptsValueSettings() {
        return true;
    }

    default void onShortInteract(PlayerEntity player, Hand hand, Direction side, BlockHitResult hitResult) {
    }

    default boolean mayInteract(PlayerEntity player) {
        return true;
    }

    default int netId() {
        return 0;
    }

    ValueSettings getValueSettings();

    default void setValueSettings(PlayerEntity player, ValueSettings valueSetting, boolean ctrlDown) {
    }

    default void playFeedbackSound(BlockEntityBehaviour<?> origin) {
        origin.getWorld().playSound(null, origin.getPos(), SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.25f, 2f);
        origin.getWorld().playSound(null, origin.getPos(), SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE.value(), SoundCategory.BLOCKS, 0.03f, 1.125f);
    }

    @Override
    default String getClipboardKey() {
        return "Settings";
    }

    @Override
    default boolean canWrite(RegistryWrapper.WrapperLookup registries, Direction side) {
        return acceptsValueSettings();
    }

    @Override
    default boolean writeToClipboard(WriteView view, Direction side) {
        if (!acceptsValueSettings())
            return false;
        ValueSettings valueSettings = getValueSettings();
        view.putInt("Value", valueSettings.value());
        view.putInt("Row", valueSettings.row());
        return true;
    }

    @Override
    default boolean readFromClipboard(ReadView view, PlayerEntity player, Direction side, boolean simulate) {
        if (!acceptsValueSettings())
            return false;
        Optional<Integer> row = view.getOptionalInt("Row");
        if (row.isEmpty()) {
            return false;
        }
        Optional<Integer> value = view.getOptionalInt("Value");
        if (value.isEmpty()) {
            return false;
        }
        if (simulate)
            return true;
        setValueSettings(player, new ValueSettings(row.get(), value.get()), false);
        return true;
    }
}
