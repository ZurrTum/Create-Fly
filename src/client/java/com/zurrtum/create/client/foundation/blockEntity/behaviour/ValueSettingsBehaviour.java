package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface ValueSettingsBehaviour {

    boolean testHit(Vec3d hit);

    boolean isActive();

    default boolean onlyVisibleWithWrench() {
        return false;
    }

    default void newSettingHovered(ValueSettings valueSetting) {
    }

    ValueBoxTransform getSlotPositioning();

    ValueSettingsBoard createBoard(PlayerEntity player, BlockHitResult hitResult);

    ValueSettings getValueSettings();

    void setValueSettings(PlayerEntity player, ValueSettings valueSetting, boolean ctrlDown);

    void onShortInteract(PlayerEntity player, Hand hand, Direction side, BlockHitResult hitResult);

    default boolean bypassesInput(ItemStack mainhandItem) {
        return false;
    }

    boolean mayInteract(PlayerEntity player);

    default boolean acceptsValueSettings() {
        return true;
    }

    int netId();
}
