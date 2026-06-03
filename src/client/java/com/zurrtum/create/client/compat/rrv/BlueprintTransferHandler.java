package com.zurrtum.create.client.compat.rrv;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintScreen;
import com.zurrtum.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.zurrtum.create.infrastructure.component.AttributeFilterWhitelistMode;
import com.zurrtum.create.infrastructure.component.ItemAttributeEntry;
import com.zurrtum.create.infrastructure.packet.c2s.BlueprintAssignCompleteRecipePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlueprintTransferHandler implements RecipeTransferHandler {
    @Override
    public boolean checkApplicable(Screen screen, ReliableClientRecipeType type) {
        return screen instanceof BlueprintScreen;
    }

    @Override
    public boolean handle(Screen screen, ReliableClientRecipe current, RecipeButton button, boolean craft) {
        if (craft) {
            SlotFillContext context = new SlotFillContext();
            current.bindSlots(context);
            List<ItemStack> input = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                SlotContent ingredient = context.contentBySlot(i);
                List<ItemStack> items = ingredient.getValidContents();
                int size = items.size();
                if (size == 0) {
                    input.add(ItemStack.EMPTY);
                    continue;
                }
                if (size == 1) {
                    input.add(items.getFirst());
                    continue;
                }
                Optional<TagKey<Item>> tag = ingredient.itemTag();
                if (tag.isPresent()) {
                    ItemStack filterItem = AllItems.ATTRIBUTE_FILTER.getDefaultInstance();
                    filterItem.set(
                        AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE,
                        AttributeFilterWhitelistMode.WHITELIST_DISJ
                    );
                    filterItem.set(
                        AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES,
                        List.of(new ItemAttributeEntry(new InTagAttribute(tag.get()), false))
                    );
                    input.add(filterItem);
                    continue;
                }
                ItemStack filterItem = AllItems.FILTER.getDefaultInstance();
                filterItem.set(AllDataComponents.FILTER_ITEMS, ItemContainerContents.fromItems(items));
                input.add(filterItem);
            }
            List<ItemStack> items = context.contentBySlot(9).getValidContents();
            if (items.isEmpty()) {
                return false;
            }
            Minecraft.getInstance().player.connection.send(new BlueprintAssignCompleteRecipePacket(
                input,
                items.getFirst()
            ));
        }
        return true;
    }
}
