package com.zurrtum.create.foundation.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ComponentsIngredient extends Ingredient {
    public static final String TYPE_KEY = "fabric:type";
    public static final Identifier ID = Identifier.fromNamespaceAndPath("fabric", "components");
    public static final String STRING_ID = ID.toString();
    public static final Codec<ComponentsIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.optionalFieldOf(TYPE_KEY).forGetter(i -> Optional.of(ID)),
        Ingredient.CODEC.fieldOf("base").forGetter(ComponentsIngredient::getBase),
        DataComponentPatch.CODEC.fieldOf("components").forGetter(ComponentsIngredient::getComponents)
    ).apply(instance, (id, base, components) -> new ComponentsIngredient(base, components)));
    public static final StreamCodec<RegistryFriendlyByteBuf, ComponentsIngredient> CONTENTS_STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC,
        ComponentsIngredient::getBase,
        DataComponentPatch.STREAM_CODEC,
        ComponentsIngredient::getComponents,
        ComponentsIngredient::new
    );
    private final Ingredient base;
    private final DataComponentPatch components;

    @SuppressWarnings("deprecation")
    public ComponentsIngredient(Ingredient base, DataComponentPatch components) {
        // We must pass a registry entry list that contains something that isn't air. It doesn't actually get used.
        super(HolderSet.direct(Items.STONE.builtInRegistryHolder()));

        if (components.isEmpty()) {
            throw new IllegalArgumentException("ComponentIngredient must have at least one defined component");
        }

        this.base = base;
        this.components = components;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Stream<Holder<Item>> items() {
        return base.items();
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
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
            final DataComponentType<?> type = entry.getKey();
            final Optional<?> value = entry.getValue();

            if (value.isPresent()) {
                // Expect the stack to contain a matching component
                if (!stack.has(type)) {
                    return false;
                }

                if (!Objects.equals(value.get(), stack.get(type))) {
                    return false;
                }
            } else {
                // Expect the target stack to not contain this component
                if (stack.has(type)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean acceptsItem(Holder<Item> registryEntry) {
        return base.acceptsItem(registryEntry);
    }

    private Ingredient getBase() {
        return base;
    }

    @Nullable
    private DataComponentPatch getComponents() {
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
    public SlotDisplay display() {
        return new SlotDisplay.Composite(base.items().map(this::createEntryDisplay).toList());
    }

    private SlotDisplay createEntryDisplay(Holder<Item> entry) {
        ItemStack stack = entry.value().getDefaultInstance();
        stack.applyComponentsAndValidate(components);
        return new SlotDisplay.ItemStackSlotDisplay(stack);
    }
}
