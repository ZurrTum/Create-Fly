package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintScreen;
import com.zurrtum.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.zurrtum.create.infrastructure.component.AttributeFilterWhitelistMode;
import com.zurrtum.create.infrastructure.component.ItemAttributeEntry;
import com.zurrtum.create.infrastructure.packet.c2s.BlueprintAssignCompleteRecipePacket;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlueprintTransferHandler implements TransferHandler {
    @Override
    public ApplicabilityResult checkApplicable(Context context) {
        if (context.getContainerScreen() instanceof BlueprintScreen && context.getDisplay().getCategoryIdentifier().equals(BuiltinPlugin.CRAFTING)) {
            return ApplicabilityResult.createApplicable();
        }
        return ApplicabilityResult.createNotApplicable();
    }

    @Override
    public Result handle(Context context) {
        if (context.isActuallyCrafting()) {
            CraftingDisplay display = (CraftingDisplay) context.getDisplay();
            List<ItemStack> input = new ArrayList<>();
            List<TagKey<Item>> cache = new ArrayList<>();
            List<InputIngredient<EntryStack<?>>> entries = display.getInputIngredients(context.getMenu(), context.getMinecraft().player);
            for (InputIngredient<EntryStack<?>> inputIngredient : entries) {
                List<EntryStack<?>> ingredient = inputIngredient.get();
                int size = ingredient.size();
                if (size == 0) {
                    input.add(ItemStack.EMPTY);
                    continue;
                }
                if (size == 1) {
                    input.add(ingredient.getFirst().castValue());
                    continue;
                }
                TagKey<Item> tag = findTag(ingredient, cache);
                if (tag != null) {
                    ItemStack filterItem = AllItems.ATTRIBUTE_FILTER.getDefaultStack();
                    filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
                    filterItem.set(
                        AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES,
                        List.of(new ItemAttributeEntry(new InTagAttribute(tag), false))
                    );
                    input.add(filterItem);
                    continue;
                }
                ItemStack filterItem = AllItems.FILTER.getDefaultStack();
                List<ItemStack> items = new ArrayList<>(size);
                for (EntryStack<?> stack : ingredient) {
                    items.add(stack.castValue());
                }
                filterItem.set(AllDataComponents.FILTER_ITEMS, ContainerComponent.fromStacks(items));
                input.add(filterItem);
            }
            ItemStack output = display.getOutputEntries().getFirst().getFirst().castValue();
            BlueprintAssignCompleteRecipePacket packet = new BlueprintAssignCompleteRecipePacket(input, output);
            context.getMinecraft().player.networkHandler.sendPacket(packet);
        }
        return Result.createSuccessful().blocksFurtherHandling();
    }

    @Nullable
    public static TagKey<Item> findTag(List<EntryStack<?>> ingredient, List<TagKey<Item>> cache) {
        List<RegistryEntry.Reference<Item>> list = getEntries(ingredient);
        for (TagKey<Item> tag : cache) {
            if (matchTag(list, tag)) {
                return tag;
            }
        }
        int size = list.size();
        return Registries.ITEM.streamTags().filter(set -> set.size() == size).map(RegistryEntryList.Named::getTag).filter(t -> matchTag(list, t))
            .findFirst().map(tag -> {
                cache.add(tag);
                return tag;
            }).orElse(null);
    }

    @SuppressWarnings("deprecation")
    public static List<RegistryEntry.Reference<Item>> getEntries(List<EntryStack<?>> ingredient) {
        List<RegistryEntry.Reference<Item>> list = new ArrayList<>(ingredient.size());
        for (EntryStack<?> stack : ingredient) {
            list.add(stack.<ItemStack>castValue().getItem().getRegistryEntry());
        }
        return list;
    }

    public static boolean matchTag(List<RegistryEntry.Reference<Item>> list, TagKey<Item> tag) {
        for (RegistryEntry.Reference<Item> entry : list) {
            if (entry.isIn(tag)) {
                continue;
            }
            return false;
        }
        return true;
    }
}
