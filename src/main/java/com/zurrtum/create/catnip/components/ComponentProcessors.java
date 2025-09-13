package com.zurrtum.create.catnip.components;

import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public class ComponentProcessors {
    public static ItemStack withUnsafeComponentsDiscarded(ItemStack stack) {
        if (stack.getComponentChanges().isEmpty())
            return stack;
        ItemStack copy = stack.copy();
        stack.getComponents().stream().filter(ComponentProcessors::isUnsafeItemComponent).map(Component::type).forEach(copy::remove);
        return copy;
    }

    public static boolean isUnsafeItemComponent(Component<?> component) {
        return isUnsafeItemComponent(component.type());
    }

    public static boolean isUnsafeItemComponent(ComponentType<?> component) {
        if (component.equals(DataComponentTypes.ENCHANTMENTS))
            return false;
        if (component.equals(DataComponentTypes.POTION_CONTENTS))
            return false;
        if (component.equals(DataComponentTypes.DAMAGE))
            return false;
        return !component.equals(DataComponentTypes.CUSTOM_NAME);
    }
}
