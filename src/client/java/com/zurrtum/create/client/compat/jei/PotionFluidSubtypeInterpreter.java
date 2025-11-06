package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.component.BottleType;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PotionFluidSubtypeInterpreter implements ISubtypeInterpreter<IJeiFluidIngredient> {
    public static final PotionFluidSubtypeInterpreter INSTANCE = new PotionFluidSubtypeInterpreter();

    @Override
    public @Nullable Object getSubtypeData(IJeiFluidIngredient ingredient, UidContext context) {
        ComponentMap components = ingredient.getFluidVariant().getComponentMap();
        if (components.isEmpty()) {
            return null;
        }
        return List.of(
            components.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT),
            components.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR)
        );
    }
}
