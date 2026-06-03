package com.zurrtum.create.client.compat.rrv;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.common.recipe.ItemViewRecipes;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.AdditionalStackModifier;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.OptionalSlotRenderer;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class CreateView implements ReliableClientRecipe {
    public static final OptionalSlotRenderer SLOT = (context, x, y, pt) -> AllGuiTextures.JEI_SLOT.render(
        context,
        0,
        0
    );
    public static final OptionalSlotRenderer CHANCE_SLOT = (context, x, y, pt) -> AllGuiTextures.JEI_CHANCE_SLOT.render(context,
        0,
        0
    );
    public static final AdditionalStackModifier NOT_CONSUMED = (stack, tooltip) -> tooltip.add(
        1,
        CreateLang.translateDirect("recipe.deploying.not_consumed").withStyle(ChatFormatting.GOLD)
    );

    public void placeSlots(SlotDefinition slotDefinition) {
        for (int i = placeViewSlots(slotDefinition), size = getType().getSlotCount(); i < size; i++) {
            slotDefinition.addItemSlot(i, 0, 0);
        }
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        for (int i = bindViewSlots(slotFillContext), size = getType().getSlotCount(); i < size; i++) {
            slotFillContext.bindOptionalSlot(i, SlotContent.of(), OptionalSlotRenderer.DEFAULT);
        }
    }

    protected int placeViewSlots(SlotDefinition slotDefinition) {
        return 0;
    }

    protected int bindViewSlots(SlotFillContext slotFillContext) {
        return 0;
    }

    public void bindChanceSlot(SlotFillContext slotFillContext, int i, SlotContent content, float chance) {
        Component text = CreateLang.translateDirect(
            "recipe.processing.chance",
            chance < 0.01 ? "<1" : (int) (chance * 100)
        ).withStyle(ChatFormatting.GOLD);
        slotFillContext.bindOptionalSlot(i, content, CHANCE_SLOT);
        slotFillContext.addAdditionalStackModifier(i, (stack, tooltip) -> tooltip.add(1, text));
    }

    @Override
    public boolean redirectsAsIngredient(ItemStack stack) {
        Item item = stack.getItem();
        List<SlotContent> ingredients = getIngredients();
        return matchPotion(item, stack, ingredients) && matchEnchantments(item, stack, ingredients);
    }

    @Override
    public boolean redirectsAsResult(ItemStack stack) {
        Item item = stack.getItem();
        List<SlotContent> results = getResults();
        return matchPotion(item, stack, results) && matchEnchantments(item, stack, results);
    }

    private static boolean matchPotion(Item item, ItemStack stack, List<SlotContent> slotContents) {
        PotionContents component = stack.get(DataComponents.POTION_CONTENTS);
        if (component == null) {
            return true;
        }
        Holder<Potion> potion = component.potion().orElse(null);
        BottleType bottleType = potion != null ? stack.get(AllDataComponents.POTION_FLUID_BOTTLE_TYPE) : null;
        for (SlotContent slotContent : slotContents) {
            for (ItemStack validStack : slotContent.getValidContents()) {
                if (validStack.is(item)) {
                    PotionContents validComponent = validStack.get(DataComponents.POTION_CONTENTS);
                    if (validComponent == null) {
                        return true;
                    }
                    if (potion == null) {
                        if (validComponent.potion().isEmpty()) {
                            return true;
                        }
                    } else if (validComponent.is(potion) && (bottleType == null || validStack.get(AllDataComponents.POTION_FLUID_BOTTLE_TYPE) == bottleType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean matchEnchantments(Item item, ItemStack stack, List<SlotContent> slotContents) {
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) {
            return true;
        }
        int size = enchantments.size();
        Set<Holder<Enchantment>> entries = enchantments.keySet();
        for (SlotContent slotContent : slotContents) {
            for (ItemStack validStack : slotContent.getValidContents()) {
                if (validStack.is(item)) {
                    ItemEnchantments validEnchantments = validStack.get(DataComponents.ENCHANTMENTS);
                    if (validEnchantments == null) {
                        return true;
                    }
                    if (validEnchantments.size() != size) {
                        continue;
                    }
                    if (matchEnchantments(entries, enchantments, validEnchantments)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean matchEnchantments(
        Set<Holder<Enchantment>> entries,
        ItemEnchantments enchantments,
        ItemEnchantments validEnchantments
    ) {
        for (Holder<Enchantment> enchantment : entries) {
            if (validEnchantments.getLevel(enchantment) != enchantments.getLevel(enchantment)) {
                return false;
            }
        }
        return true;
    }

    public static SlotContent createSlot(SizedIngredient sizedIngredient) {
        int size = sizedIngredient.getCount();
        Ingredient ingredient = sizedIngredient.getIngredient();
        if (size == 1) {
            return SlotContent.of(ingredient);
        }
        List<ItemStack> itemStacks = ingredient.display()
            .resolveForStacks(SlotDisplayContext.fromLevel(Minecraft.getInstance().level));
        for (ItemStack stack : itemStacks) {
            stack.setCount(size);
        }
        SlotContent slotContent = SlotContent.of(itemStacks);
        ingredient.values.unwrap().ifLeft(slotContent::bindItemTag);
        return slotContent;
    }

    public static SlotContent createSlot(FluidIngredient ingredient) {
        List<Fluid> fluids = ingredient.getMatchingFluids();
        int size = fluids.size();
        List<ItemStack> list = new ArrayList<>(size);
        int amount = ingredient.amount();
        DataComponentPatch components = null;
        if (ingredient instanceof FluidStackIngredient stackIngredient) {
            components = stackIngredient.components();
        }
        for (int i = 0; i < size; i++) {
            Fluid fluid = fluids.get(i);
            Item item = ItemViewRecipes.INSTANCE.itemForFluid(fluid);
            if (item == Items.AIR) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            if (components != null) {
                stack.applyComponents(components);
                updatePotionName(fluid, stack);
            }
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putInt("fluidAmount", amount);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
            list.add(stack);
        }
        return SlotContent.of(list);
    }

    public static SlotContent createSlot(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        Item item = ItemViewRecipes.INSTANCE.itemForFluid(fluid);
        if (item == Items.AIR) {
            return SlotContent.of();
        }
        ItemStack stack = new ItemStack(item);
        stack.applyComponents(fluidStack.getComponents());
        updatePotionName(fluid, stack);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("fluidAmount", fluidStack.getAmount());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        return SlotContent.of(stack);
    }

    private static void updatePotionName(Fluid fluid, ItemStack stack) {
        if (fluid != AllFluids.POTION) {
            return;
        }
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        BottleType bottleType = stack.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
        Component name = contents.getName(PotionFluidHandler.itemFromBottleType(bottleType)
            .getDescriptionId() + ".effect.");
        stack.set(DataComponents.ITEM_NAME, name);
        if (!stack.has(DataComponents.POTION_DURATION_SCALE) && bottleType == BottleType.LINGERING) {
            stack.set(
                DataComponents.POTION_DURATION_SCALE,
                Items.LINGERING_POTION.components().get(DataComponents.POTION_DURATION_SCALE)
            );
        }
    }
}
