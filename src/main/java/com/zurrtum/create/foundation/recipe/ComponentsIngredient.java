package com.zurrtum.create.foundation.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ComponentsIngredient extends Ingredient {
    public static final String TYPE_KEY = "fabric:type";
    public static final Identifier ID = Identifier.of("fabric", "components");
    public static final String STRING_ID = ID.toString();
    public static final Codec<ComponentsIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.optionalFieldOf(TYPE_KEY).forGetter(i -> Optional.of(ID)),
        Ingredient.CODEC.fieldOf("base").forGetter(ComponentsIngredient::getBase),
        ComponentChanges.CODEC.fieldOf("components").forGetter(ComponentsIngredient::getComponents)
    ).apply(instance, (id, base, components) -> new ComponentsIngredient(base, components)));
    public static final PacketCodec<RegistryByteBuf, ComponentsIngredient> PACKET_CODEC = PacketCodec.tuple(
        Ingredient.PACKET_CODEC,
        ComponentsIngredient::getBase,
        ComponentChanges.PACKET_CODEC,
        ComponentsIngredient::getComponents,
        ComponentsIngredient::new
    );
    private final Ingredient base;
    private final ComponentChanges components;

    @SuppressWarnings("deprecation")
    public ComponentsIngredient(Ingredient base, ComponentChanges components) {
        // We must pass a registry entry list that contains something that isn't air. It doesn't actually get used.
        super(RegistryEntryList.of(Items.STONE.getRegistryEntry()));

        if (components.isEmpty()) {
            throw new IllegalArgumentException("ComponentIngredient must have at least one defined component");
        }

        this.base = base;
        this.components = components;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Stream<RegistryEntry<Item>> getMatchingItems() {
        return base.getMatchingItems();
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!base.test(stack))
            return false;

        // None strict matching
        for (Map.Entry<ComponentType<?>, Optional<?>> entry : components.entrySet()) {
            final ComponentType<?> type = entry.getKey();
            final Optional<?> value = entry.getValue();

            if (value.isPresent()) {
                // Expect the stack to contain a matching component
                if (!stack.contains(type)) {
                    return false;
                }

                if (!Objects.equals(value.get(), stack.get(type))) {
                    return false;
                }
            } else {
                // Expect the target stack to not contain this component
                if (stack.contains(type)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean acceptsItem(RegistryEntry<Item> registryEntry) {
        return base.acceptsItem(registryEntry);
    }

    private Ingredient getBase() {
        return base;
    }

    @Nullable
    private ComponentChanges getComponents() {
        return components;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ComponentsIngredient that = (ComponentsIngredient) o;
        return base.equals(that.base) && components.equals(that.components);
    }

    @Override
    public SlotDisplay toDisplay() {
        return new SlotDisplay.CompositeSlotDisplay(base.getMatchingItems().map(this::createEntryDisplay).toList());
    }

    private SlotDisplay createEntryDisplay(RegistryEntry<Item> entry) {
        ItemStack stack = entry.value().getDefaultStack();
        stack.applyChanges(components);
        return new SlotDisplay.StackSlotDisplay(stack);
    }
}
