package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.infrastructure.component.AttributeFilterWhitelistMode;
import com.zurrtum.create.infrastructure.component.ItemAttributeEntry;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class AttributeFilterMenu extends AbstractFilterMenu {

    public AttributeFilterWhitelistMode whitelistMode;
    public List<ItemAttributeEntry> selectedAttributes;

    public AttributeFilterMenu(int id, Inventory inv, ItemStack stack) {
        super(AllMenuTypes.ATTRIBUTE_FILTER, id, inv, stack);
    }

    public void appendSelectedAttribute(ItemAttribute itemAttribute, boolean inverted) {
        selectedAttributes.add(new ItemAttributeEntry(itemAttribute, inverted));
    }

    @Override
    protected void init(Inventory inv, ItemStack contentHolder) {
        super.init(inv, contentHolder);
        ItemStack stack = new ItemStack(Items.NAME_TAG);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Selected Tags").withStyle(ChatFormatting.RESET, ChatFormatting.BLUE));
        ghostInventory.setItem(1, stack);
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
            public boolean mayPickup(Player playerIn) {
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
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId == 37)
            return;
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canDragTo(Slot slotIn) {
        if (slotIn.index == 37)
            return false;
        return super.canDragTo(slotIn);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
        if (slotIn.index == 37)
            return false;
        return super.canTakeItemForPickAll(stack, slotIn);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot slot = slots.get(index);
        ItemStack stackToInsert = slot.getItem();
        ItemStack copy = stackToInsert.copy();
        copy.setCount(1);
        ghostInventory.setItem(0, copy);
        return ItemStack.EMPTY;
    }

    @Override
    protected void initAndReadInventory(ItemStack filterItem) {
        super.initAndReadInventory(filterItem);
        selectedAttributes = new ArrayList<>();
        whitelistMode = filterItem.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
        List<ItemAttributeEntry> attributes = filterItem.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, List.of());
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