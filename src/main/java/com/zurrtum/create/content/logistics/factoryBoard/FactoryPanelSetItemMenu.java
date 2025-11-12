package com.zurrtum.create.content.logistics.factoryBoard;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class FactoryPanelSetItemMenu extends GhostItemMenu<ServerFactoryPanelBehaviour> {

    public FactoryPanelSetItemMenu(int id, Inventory inv, ServerFactoryPanelBehaviour contentHolder) {
        super(AllMenuTypes.FACTORY_PANEL_SET_ITEM, id, inv, contentHolder);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler();
    }

    @Override
    protected boolean allowRepeats() {
        return true;
    }

    @Override
    protected void addSlots() {
        int playerX = 13;
        int playerY = 112;
        int slotX = 74;
        int slotY = 28;

        addPlayerSlots(playerX, playerY);
        addSlot(new Slot(ghostInventory, 0, slotX, slotY));
    }

    @Override
    protected void saveData(ServerFactoryPanelBehaviour contentHolder) {
        if (!contentHolder.setFilter(ghostInventory.getItem(0))) {
            player.displayClientMessage(Component.translatable("create.logistics.filter.invalid_item"), true);
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }
        player.level().playSound(null, contentHolder.getPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }

}
