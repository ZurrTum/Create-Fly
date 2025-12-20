package com.zurrtum.create.client.compat.eiv;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import de.crafty.eiv.common.api.recipe.IEivViewRecipe;
import de.crafty.eiv.common.recipe.ItemViewRecipes;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.AdditionalStackModifier;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.OptionalSlotRenderer;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotDefinition;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public abstract class CreateView extends AbstractList<IEivViewRecipe> implements IEivViewRecipe {
    public static final OptionalSlotRenderer SLOT = (context, x, y, pt) -> AllGuiTextures.JEI_SLOT.render(context, 0, 0);
    public static final OptionalSlotRenderer CHANCE_SLOT = (context, x, y, pt) -> AllGuiTextures.JEI_CHANCE_SLOT.render(context, 0, 0);
    public static final AdditionalStackModifier NOT_CONSUMED = (stack, tooltip) -> tooltip.add(CreateLang.translateDirect(
        "recipe.deploying.not_consumed").formatted(Formatting.GOLD));

    public void placeSlots(SlotDefinition slotDefinition) {
        for (int i = placeViewSlots(slotDefinition), size = getViewType().getSlotCount(); i < size; i++) {
            slotDefinition.addItemSlot(i, 0, 0);
        }
    }

    @Override
    public void bindSlots(SlotFillContext slotFillContext) {
        for (int i = bindViewSlots(slotFillContext), size = getViewType().getSlotCount(); i < size; i++) {
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
        Text text = CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100)).formatted(Formatting.GOLD);
        slotFillContext.bindOptionalSlot(i, content, CHANCE_SLOT);
        slotFillContext.addAdditionalStackModifier(i, (stack, tooltip) -> tooltip.add(text));
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
        PotionContentsComponent component = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (component == null) {
            return true;
        }
        RegistryEntry<Potion> potion = component.potion().orElse(null);
        BottleType bottleType = potion != null ? stack.get(AllDataComponents.POTION_FLUID_BOTTLE_TYPE) : null;
        for (SlotContent slotContent : slotContents) {
            for (ItemStack validStack : slotContent.getValidContents()) {
                if (validStack.isOf(item)) {
                    PotionContentsComponent validComponent = validStack.get(DataComponentTypes.POTION_CONTENTS);
                    if (validComponent == null) {
                        return true;
                    }
                    if (potion == null) {
                        if (validComponent.potion().isEmpty()) {
                            return true;
                        }
                    } else if (validComponent.matches(potion) && (bottleType == null || validStack.get(AllDataComponents.POTION_FLUID_BOTTLE_TYPE) == bottleType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean matchEnchantments(Item item, ItemStack stack, List<SlotContent> slotContents) {
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null) {
            return true;
        }
        int size = enchantments.getSize();
        Set<RegistryEntry<Enchantment>> entries = enchantments.getEnchantments();
        for (SlotContent slotContent : slotContents) {
            for (ItemStack validStack : slotContent.getValidContents()) {
                if (validStack.isOf(item)) {
                    ItemEnchantmentsComponent validEnchantments = validStack.get(DataComponentTypes.ENCHANTMENTS);
                    if (validEnchantments == null) {
                        return true;
                    }
                    if (validEnchantments.getSize() != size) {
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
        Set<RegistryEntry<Enchantment>> entries,
        ItemEnchantmentsComponent enchantments,
        ItemEnchantmentsComponent validEnchantments
    ) {
        for (RegistryEntry<Enchantment> enchantment : entries) {
            if (validEnchantments.getLevel(enchantment) != enchantments.getLevel(enchantment)) {
                return false;
            }
        }
        return true;
    }

    public static List<ItemStack> getItemStacks(FluidIngredient ingredient) {
        List<Fluid> fluids = ingredient.getMatchingFluids();
        List<ItemStack> list = new ArrayList<>(fluids.size());
        int amount = ingredient.amount();
        ComponentChanges components = null;
        if (ingredient instanceof FluidStackIngredient stackIngredient) {
            components = stackIngredient.components();
        }
        for (Fluid fluid : fluids) {
            Item item = ItemViewRecipes.INSTANCE.itemForFluid(fluid);
            if (item == Items.AIR) {
                continue;
            }
            ItemStack stack = item.getDefaultStack();
            if (components != null) {
                stack.applyUnvalidatedChanges(components);
                updatePotionName(fluid, stack);
            }
            NbtCompound tag = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
            tag.putInt("fluidAmount", amount);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
            list.add(stack);
        }
        return list;
    }

    public static ItemStack getItemStack(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        Item item = ItemViewRecipes.INSTANCE.itemForFluid(fluid);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = item.getDefaultStack();
        stack.applyComponentsFrom(fluidStack.getComponents());
        updatePotionName(fluid, stack);
        NbtCompound tag = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        tag.putInt("fluidAmount", fluidStack.getAmount());
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
        return stack;
    }

    private static void updatePotionName(Fluid fluid, ItemStack stack) {
        if (fluid != AllFluids.POTION) {
            return;
        }
        PotionContentsComponent contents = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        BottleType bottleType = stack.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
        Text name = contents.getName(PotionFluidHandler.itemFromBottleType(bottleType).getTranslationKey() + ".effect.");
        stack.set(DataComponentTypes.ITEM_NAME, name);
        if (!stack.contains(DataComponentTypes.POTION_DURATION_SCALE) && bottleType == BottleType.LINGERING) {
            stack.set(DataComponentTypes.POTION_DURATION_SCALE, Items.LINGERING_POTION.getComponents().get(DataComponentTypes.POTION_DURATION_SCALE));
        }
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Iterator<IEivViewRecipe> iterator() {
        return new ViewIterator();
    }

    @Override
    public boolean contains(Object o) {
        return o == this;
    }

    @Override
    public IEivViewRecipe get(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
        } else {
            return this;
        }
    }

    @Override
    public void forEach(Consumer<? super IEivViewRecipe> action) {
        action.accept(this);
    }

    public boolean removeIf(Predicate<? super IEivViewRecipe> filter) {
        throw new UnsupportedOperationException();
    }

    public void replaceAll(UnaryOperator<IEivViewRecipe> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort(Comparator<? super IEivViewRecipe> c) {
    }

    @Override
    public Spliterator<IEivViewRecipe> spliterator() {
        return new ViewSpliterator();
    }

    private class ViewSpliterator implements Spliterator<IEivViewRecipe> {
        long est = 1L;

        public Spliterator<IEivViewRecipe> trySplit() {
            return null;
        }

        public boolean tryAdvance(Consumer<? super IEivViewRecipe> consumer) {
            Objects.requireNonNull(consumer);
            if (est > 0L) {
                --est;
                consumer.accept(CreateView.this);
                return true;
            } else {
                return false;
            }
        }

        public void forEachRemaining(Consumer<? super IEivViewRecipe> consumer) {
            tryAdvance(consumer);
        }

        public long estimateSize() {
            return est;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.SUBSIZED;
        }
    }

    private class ViewIterator implements Iterator<IEivViewRecipe> {
        private boolean hasNext = true;

        public boolean hasNext() {
            return hasNext;
        }

        public IEivViewRecipe next() {
            if (hasNext) {
                hasNext = false;
                return CreateView.this;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void forEachRemaining(Consumer<? super IEivViewRecipe> action) {
            Objects.requireNonNull(action);
            if (hasNext) {
                hasNext = false;
                action.accept(CreateView.this);
            }
        }
    }
}
