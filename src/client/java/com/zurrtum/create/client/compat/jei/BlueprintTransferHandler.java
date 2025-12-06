package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.equipment.blueprint.BlueprintMenu;
import com.zurrtum.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.zurrtum.create.infrastructure.component.AttributeFilterWhitelistMode;
import com.zurrtum.create.infrastructure.component.ItemAttributeEntry;
import com.zurrtum.create.infrastructure.packet.c2s.BlueprintAssignCompleteRecipePacket;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlueprintTransferHandler implements IRecipeTransferHandler<BlueprintMenu, RecipeEntry<CraftingRecipe>> {
    @Override
    public Class<? extends BlueprintMenu> getContainerClass() {
        return BlueprintMenu.class;
    }

    @Override
    public Optional<ScreenHandlerType<BlueprintMenu>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public IRecipeType<RecipeEntry<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
        BlueprintMenu menu,
        RecipeEntry<CraftingRecipe> craftingRecipe,
        IRecipeSlotsView recipeSlots,
        PlayerEntity player,
        boolean maxTransfer,
        boolean doTransfer
    ) {
        if (!doTransfer)
            return null;

        List<IRecipeSlotView> inputViews = new ArrayList<>();
        List<IRecipeSlotView> outputViews = new ArrayList<>();
        for (IRecipeSlotView view : recipeSlots.getSlotViews()) {
            RecipeIngredientRole role = view.getRole();
            if (role == RecipeIngredientRole.INPUT) {
                inputViews.add(view);
            } else if (role == RecipeIngredientRole.OUTPUT) {
                outputViews.add(view);
            }
        }
        ItemStack output = null;
        for (IRecipeSlotView view : outputViews) {
            Optional<ItemStack> stack = view.getDisplayedItemStack();
            if (stack.isPresent()) {
                output = stack.get();
                break;
            }
        }
        if (output == null) {
            return new RecipeTransferErrorMissingSlots(Text.translatable("jei.tooltip.error.recipe.transfer.missing"), outputViews);
        }
        List<TagKey<Item>> cache = new ArrayList<>();
        List<ItemStack> input = new ArrayList<>();
        for (IRecipeSlotView view : inputViews) {
            List<ItemStack> ingredient = view.getItemStacks().toList();
            int size = ingredient.size();
            if (size == 0) {
                input.add(ItemStack.EMPTY);
                continue;
            }
            if (size == 1) {
                input.add(ingredient.getFirst());
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
            filterItem.set(AllDataComponents.FILTER_ITEMS, ContainerComponent.fromStacks(ingredient));
            input.add(filterItem);
        }
        BlueprintAssignCompleteRecipePacket packet = new BlueprintAssignCompleteRecipePacket(input, output);
        ((ClientPlayerEntity) player).networkHandler.sendPacket(packet);
        return null;
    }

    @Nullable
    public static TagKey<Item> findTag(List<ItemStack> ingredient, List<TagKey<Item>> cache) {
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
    public static List<RegistryEntry.Reference<Item>> getEntries(List<ItemStack> ingredient) {
        List<RegistryEntry.Reference<Item>> list = new ArrayList<>(ingredient.size());
        for (ItemStack stack : ingredient) {
            list.add(stack.getItem().getRegistryEntry());
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
