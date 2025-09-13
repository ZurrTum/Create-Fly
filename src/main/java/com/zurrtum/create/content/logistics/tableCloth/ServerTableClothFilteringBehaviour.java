package com.zurrtum.create.content.logistics.tableCloth;

import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class ServerTableClothFilteringBehaviour extends ServerFilteringBehaviour {
    public ServerTableClothFilteringBehaviour(SmartBlockEntity be) {
        super(be);
        withPredicate(is -> !(is.getItem() instanceof FilterItem) && !(is.getItem() instanceof ShoppingListItem));
        count = 1;
    }

    @Override
    public void onShortInteract(PlayerEntity player, Hand hand, Direction side, BlockHitResult hitResult) {
        super.onShortInteract(player, hand, side, hitResult);
    }

    private TableClothBlockEntity dbe() {
        return (TableClothBlockEntity) blockEntity;
    }

    @Override
    public boolean mayInteract(PlayerEntity player) {
        return dbe().owner == null || player.getUuid().equals(dbe().owner);
    }

    @Override
    public boolean isSafeNBT() {
        return false;
    }

    public boolean isCountVisible() {
        return !filter.isEmpty();
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        int before = count;
        boolean result = super.setFilter(stack);
        count = before;
        return result;
    }

    @Override
    public void setValueSettings(PlayerEntity player, ValueSettings settings, boolean ctrlDown) {
        if (getValueSettings().equals(settings))
            return;
        count = Math.max(1, settings.value());
        blockEntity.markDirty();
        blockEntity.sendData();
        playFeedbackSound(this);
    }

    @Override
    public boolean isActive() {
        return dbe().isShop();
    }
}
