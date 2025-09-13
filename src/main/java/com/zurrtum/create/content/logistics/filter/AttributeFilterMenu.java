package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.infrastructure.component.AttributeFilterWhitelistMode;
import com.zurrtum.create.infrastructure.component.ItemAttributeEntry;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class AttributeFilterMenu extends AbstractFilterMenu {

    public AttributeFilterWhitelistMode whitelistMode;
    public List<ItemAttributeEntry> selectedAttributes;

    public AttributeFilterMenu(int id, PlayerInventory inv, ItemStack stack) {
        super(AllMenuTypes.ATTRIBUTE_FILTER, id, inv, stack);
    }

    public void appendSelectedAttribute(ItemAttribute itemAttribute, boolean inverted) {
        selectedAttributes.add(new ItemAttributeEntry(itemAttribute, inverted));
    }

    @Override
    protected void init(PlayerInventory inv, ItemStack contentHolder) {
        super.init(inv, contentHolder);
        ItemStack stack = new ItemStack(Items.NAME_TAG);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Selected Tags").formatted(Formatting.RESET, Formatting.BLUE));
        ghostInventory.setStack(1, stack);
    }

    @Override
    protected int getPlayerInventoryXOffset() {
        return 51;
    }

    @Override
    protected int getPlayerInventoryYOffset() {
        return 107;
    }

    @Override
    protected void addFilterSlots() {
        this.addSlot(new Slot(ghostInventory, 0, 16, 27));
        this.addSlot(new Slot(ghostInventory, 1, 16, 62) {
            @Override
            public boolean canTakeItems(PlayerEntity playerIn) {
                return false;
            }
        });
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(2);
    }

    @Override
    public void clearContents() {
        selectedAttributes.clear();
    }

    @Override
    public void onSlotClick(int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player) {
        if (slotId == 37)
            return;
        super.onSlotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canInsertIntoSlot(Slot slotIn) {
        if (slotIn.id == 37)
            return false;
        return super.canInsertIntoSlot(slotIn);
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slotIn) {
        if (slotIn.id == 37)
            return false;
        return super.canInsertIntoSlot(stack, slotIn);
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerIn, int index) {
        if (index == 37)
            return ItemStack.EMPTY;
        if (index == 36) {
            ghostInventory.setStack(37, ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }
        if (index < 36) {
            ItemStack stackToInsert = playerInventory.getStack(index);
            ItemStack copy = stackToInsert.copy();
            copy.setCount(1);
            ghostInventory.setStack(0, copy);
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void initAndReadInventory(ItemStack filterItem) {
        super.initAndReadInventory(filterItem);
        selectedAttributes = new ArrayList<>();
        whitelistMode = filterItem.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
        List<ItemAttributeEntry> attributes = filterItem.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, new ArrayList<>());
        selectedAttributes.addAll(attributes);
    }

    @Override
    protected void saveData(ItemStack filterItem) {
        filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, whitelistMode);
        List<ItemAttributeEntry> attributes = new ArrayList<>();
        selectedAttributes.forEach(at -> {
            if (at == null)
                return;
            attributes.add(new ItemAttributeEntry(at.attribute(), at.inverted()));
        });
        filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, attributes);

        if (attributes.isEmpty() && whitelistMode == AttributeFilterWhitelistMode.WHITELIST_DISJ) {
            filterItem.remove(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES);
            filterItem.remove(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE);
        }
    }

}