package com.zurrtum.create.content.equipment.blueprint;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity.BlueprintSection;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class BlueprintMenu extends GhostItemMenu<BlueprintSection> {
    public BlueprintMenu(int id, Inventory inv, BlueprintSection section) {
        super(AllMenuTypes.CRAFTING_BLUEPRINT, id, inv, section);
    }

    @Override
    protected boolean allowRepeats() {
        return true;
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(8, 131);

        int x = 29;
        int y = 21;
        int index = 0;
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 3; ++col)
                this.addSlot(new BlueprintCraftSlot(ghostInventory, index++, x + col * 18, y + row * 18));

        addSlot(new BlueprintCraftSlot(ghostInventory, index++, 123, 40));
        addSlot(new Slot(ghostInventory, index++, 135, 57));
    }

    public void onCraftMatrixChanged() {
        Level level = contentHolder.getBlueprintWorld();
        if (level.isClientSide())
            return;

        ServerPlayer serverplayerentity = (ServerPlayer) player;
        CraftingInput input = CraftingInput.of(3, 3, ghostInventory.getStacks().subList(0, 9));
        Optional<RecipeHolder<CraftingRecipe>> optional = ((ServerLevel) level).recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level);

        if (optional.isEmpty()) {
            if (ghostInventory.getItem(9).isEmpty())
                return;
            if (!contentHolder.inferredIcon)
                return;

            ghostInventory.setItem(9, ItemStack.EMPTY);
            serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 45, ItemStack.EMPTY));
            contentHolder.inferredIcon = false;
            return;
        }

        CraftingRecipe icraftingrecipe = optional.get().value();
        ItemStack itemstack = icraftingrecipe.assemble(input);
        ghostInventory.setItem(9, itemstack);
        contentHolder.inferredIcon = true;
        ItemStack toSend = itemstack.copy();
        toSend.set(AllDataComponents.INFERRED_FROM_RECIPE, true);
        serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 45, toSend));
    }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {
        if (slotId == 45) {
            contentHolder.inferredIcon = stack.getOrDefault(AllDataComponents.INFERRED_FROM_RECIPE, false);
            stack.remove(AllDataComponents.INFERRED_FROM_RECIPE);
        }
        super.setItem(slotId, stateId, stack);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return contentHolder.getItems();
    }

    @Override
    protected void initAndReadInventory(BlueprintSection contentHolder) {
        super.initAndReadInventory(contentHolder);
    }

    @Override
    protected void saveData(BlueprintSection contentHolder) {
        contentHolder.save(ghostInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return contentHolder != null && contentHolder.canPlayerUse(player);
    }

    class BlueprintCraftSlot extends Slot {
        public BlueprintCraftSlot(Container itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            int index = getContainerSlot();
            if (index == 9) {
                if (hasItem() && !contentHolder.getBlueprintWorld().isClientSide()) {
                    contentHolder.inferredIcon = false;
                    ServerPlayer serverplayerentity = (ServerPlayer) player;
                    serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 45, getItem()));
                }
            } else if (index < 9) {
                onCraftMatrixChanged();
            }
        }

    }

}
