package com.zurrtum.create.content.logistics.factoryBoard;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class FactoryPanelSetItemMenu extends GhostItemMenu<ServerFactoryPanelBehaviour> {

    public FactoryPanelSetItemMenu(int id, PlayerInventory inv, ServerFactoryPanelBehaviour contentHolder) {
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
        if (!contentHolder.setFilter(ghostInventory.getStack(0))) {
            player.sendMessage(Text.translatable("create.logistics.filter.invalid_item"), true);
            AllSoundEvents.DENY.playOnServer(player.getEntityWorld(), player.getBlockPos(), 1, 1);
            return;
        }
        player.getEntityWorld().playSound(null, contentHolder.getPos(), SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, .25f, .1f);
    }

}
