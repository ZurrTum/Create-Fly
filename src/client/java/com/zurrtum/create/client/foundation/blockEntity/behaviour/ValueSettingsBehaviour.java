package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public interface ValueSettingsBehaviour {

    boolean testHit(Vec3 hit);

    boolean isActive();

    default boolean onlyVisibleWithWrench() {
        return false;
    }

    default void newSettingHovered(ValueSettings valueSetting) {
    }

    ValueBoxTransform getSlotPositioning();

    ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult);

    ValueSettings getValueSettings();

    void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown);

    void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult);

    default boolean bypassesInput(ItemStack mainhandItem) {
        return false;
    }

    boolean mayInteract(Player player);

    default boolean acceptsValueSettings() {
        return true;
    }

    int netId();
}
