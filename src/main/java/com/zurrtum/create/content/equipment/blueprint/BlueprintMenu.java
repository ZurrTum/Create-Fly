package com.zurrtum.create.content.equipment.blueprint;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity.BlueprintSection;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.Optional;

public class BlueprintMenu extends GhostItemMenu<BlueprintSection> {
    public BlueprintMenu(int id, PlayerInventory inv, BlueprintSection section) {
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
        World level = contentHolder.getBlueprintWorld();
        if (level.isClient)
            return;

        ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) player;
        CraftingRecipeInput input = CraftingRecipeInput.create(3, 3, ghostInventory.getStacks().subList(0, 9));
        Optional<RecipeEntry<CraftingRecipe>> optional = ((ServerWorld) level).getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, level);

        if (optional.isEmpty()) {
            if (ghostInventory.getStack(9).isEmpty())
                return;
            if (!contentHolder.inferredIcon)
                return;

            ghostInventory.setStack(9, ItemStack.EMPTY);
            serverplayerentity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), 45, ItemStack.EMPTY));
            contentHolder.inferredIcon = false;
            return;
        }

        CraftingRecipe icraftingrecipe = optional.get().value();
        ItemStack itemstack = icraftingrecipe.craft(input, level.getRegistryManager());
        ghostInventory.setStack(9, itemstack);
        contentHolder.inferredIcon = true;
        ItemStack toSend = itemstack.copy();
        toSend.set(AllDataComponents.INFERRED_FROM_RECIPE, true);
        serverplayerentity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), 45, toSend));
    }

    @Override
    public void setStackInSlot(int slotId, int stateId, ItemStack stack) {
        if (slotId == 45) {
            contentHolder.inferredIcon = stack.getOrDefault(AllDataComponents.INFERRED_FROM_RECIPE, false);
            stack.remove(AllDataComponents.INFERRED_FROM_RECIPE);
        }
        super.setStackInSlot(slotId, stateId, stack);
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
    public boolean canUse(PlayerEntity player) {
        return contentHolder != null && contentHolder.canPlayerUse(player);
    }

    class BlueprintCraftSlot extends Slot {
        public BlueprintCraftSlot(Inventory itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public void markDirty() {
            super.markDirty();
            int index = getIndex();
            if (index == 9) {
                if (hasStack() && !contentHolder.getBlueprintWorld().isClient) {
                    contentHolder.inferredIcon = false;
                    ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) player;
                    serverplayerentity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), 45, getStack()));
                }
            } else if (index < 9) {
                onCraftMatrixChanged();
            }
        }

    }

}
