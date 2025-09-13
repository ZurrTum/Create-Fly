package com.zurrtum.create.content.equipment.toolbox;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import static com.zurrtum.create.content.equipment.toolbox.ToolboxInventory.STACKS_PER_COMPARTMENT;

public class ToolboxMenu extends MenuBase<ToolboxBlockEntity> {
    public ToolboxMenu(int id, PlayerInventory inv, ToolboxBlockEntity be) {
        super(AllMenuTypes.TOOLBOX, id, inv, be);
        BlockEntityBehaviour.get(be, AnimatedContainerBehaviour.TYPE).startOpen(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot clickedSlot = getSlot(index);
        ItemStack stack = clickedSlot.getStack();
        int size = contentHolder.inventory.size();
        boolean success;
        if (index < size) {
            stack = settle(stack, index);
            if (stack.isEmpty()) {
                return stack;
            }
            success = !insertItem(stack, size, slots.size(), false);
            contentHolder.inventory.markDirty();
        } else {
            if (stack.isEmpty()) {
                return stack;
            }
            success = !insertItem(stack, 0, size, false);
        }

        return success ? ItemStack.EMPTY : stack;
    }

    public ItemStack settle(ItemStack stack, int index) {
        int count = stack.getCount();
        int space;
        if (count == 0) {
            ItemStack filter = contentHolder.inventory.filters.get(index / STACKS_PER_COMPARTMENT);
            if (filter.isEmpty()) {
                return stack;
            }
            space = filter.getMaxCount();
        } else {
            space = stack.getMaxCount() - count;
        }
        if (space != 0) {
            ItemStack extract = contentHolder.inventory.takeFromCompartment(space, index + 1, index + STACKS_PER_COMPARTMENT - 1);
            if (!extract.isEmpty()) {
                if (count == 0) {
                    stack = extract;
                    contentHolder.inventory.setStack(index, stack);
                } else {
                    stack.setCount(count + extract.getCount());
                }
            }
        }
        return stack;
    }

    @Override
    protected void initAndReadInventory(ToolboxBlockEntity contentHolder) {
    }

    @Override
    public void onSlotClick(int index, int flags, SlotActionType type, PlayerEntity player) {
        if (index >= 0 && index < contentHolder.inventory.size()) {
            ItemStack itemInClickedSlot = getSlot(index).getStack();
            ItemStack carried = getCursorStack();

            if (type == SlotActionType.PICKUP && !carried.isEmpty() && !itemInClickedSlot.isEmpty() && ToolboxInventory.canItemsShareCompartment(itemInClickedSlot,
                carried
            )) {
                int subIndex = index % STACKS_PER_COMPARTMENT;
                if (subIndex != STACKS_PER_COMPARTMENT - 1) {
                    onSlotClick(index - subIndex + STACKS_PER_COMPARTMENT - 1, flags, type, player);
                    return;
                }
            }

            if (type == SlotActionType.PICKUP && carried.isEmpty() && settle(itemInClickedSlot, index).isEmpty() && !player.getWorld().isClient) {
                contentHolder.inventory.filters.set(index / STACKS_PER_COMPARTMENT, ItemStack.EMPTY);
                contentHolder.sendData();
            }

        }
        super.onSlotClick(index, flags, type, player);
    }

    @Override
    public boolean canInsertIntoSlot(Slot slot) {
        return slot.id > contentHolder.inventory.size();
    }

    public ItemStack getFilter(int compartment) {
        return contentHolder.inventory.filters.get(compartment);
    }

    public int totalCountInCompartment(int compartment) {
        int count = 0;
        int baseSlot = compartment * STACKS_PER_COMPARTMENT;
        for (int i = 0; i < STACKS_PER_COMPARTMENT; i++)
            count += getSlot(baseSlot + i).getStack().getCount();
        return count;
    }

    public boolean renderPass;

    @Override
    protected void addSlots() {
        ToolboxInventory inventory = contentHolder.inventory;

        int x = 79;
        int y = 37;

        int[] xOffsets = {x, x + 33, x + 66, x + 66 + 6, x + 66, x + 33, x, x - 6};
        int[] yOffsets = {y, y - 6, y, y + 33, y + 66, y + 66 + 6, y + 66, y + 33};

        for (int compartment = 0; compartment < 8; compartment++) {
            int baseIndex = compartment * STACKS_PER_COMPARTMENT;

            // Representative Slots
            addSlot(new ToolboxSlot(this, inventory, baseIndex, xOffsets[compartment], yOffsets[compartment], true));

            // Hidden Slots
            for (int i = 1; i < STACKS_PER_COMPARTMENT; i++)
                addSlot(new ToolboxSlot(this, inventory, baseIndex + i, -10000, -10000, false));
        }

        addPlayerSlots(8, 165);
    }

    @Override
    protected void saveData(ToolboxBlockEntity contentHolder) {

    }

    @Override
    public void onClosed(PlayerEntity playerIn) {
        super.onClosed(playerIn);
        if (!playerIn.getWorld().isClient)
            BlockEntityBehaviour.get(contentHolder, AnimatedContainerBehaviour.TYPE).stopOpen(playerIn);
    }

}
