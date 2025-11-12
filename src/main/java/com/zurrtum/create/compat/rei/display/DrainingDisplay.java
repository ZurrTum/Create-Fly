package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.registry.display.DisplayConsumer;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

public record DrainingDisplay(
    EntryIngredient input, EntryIngredient output, EntryIngredient result, Optional<ResourceLocation> location
) implements Display {
    public static final ResourceLocation POTIONS = ResourceLocation.fromNamespaceAndPath(MOD_ID, "potions");
    public static final DisplaySerializer<DrainingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(DrainingDisplay::input),
            EntryIngredient.codec().fieldOf("output").forGetter(DrainingDisplay::output),
            EntryIngredient.codec().fieldOf("result").forGetter(DrainingDisplay::result),
            ResourceLocation.CODEC.optionalFieldOf("location").forGetter(DrainingDisplay::location)
        ).apply(instance, DrainingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            DrainingDisplay::input,
            EntryIngredient.streamCodec(),
            DrainingDisplay::output,
            EntryIngredient.streamCodec(),
            DrainingDisplay::result,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            DrainingDisplay::location,
            DrainingDisplay::new
        )
    );

    public DrainingDisplay(RecipeHolder<EmptyingRecipe> entry) {
        this(entry.id().location(), entry.value());
    }

    public DrainingDisplay(ResourceLocation id, EmptyingRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            IngredientHelper.createEntryIngredient(recipe.fluidResult()),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    public static void register(Stream<EntryStack<?>> itemStream, DisplayConsumer registry) {
        itemStream.forEach(entry -> {
            ItemStack stack = entry.castValue();
            if (PotionFluidHandler.isPotionItem(stack)) {
                registry.add(new DrainingDisplay(
                    EntryIngredients.of(stack),
                    IngredientHelper.createEntryIngredient(PotionFluidHandler.getFluidFromPotionItem(stack)),
                    EntryIngredients.of(Items.GLASS_BOTTLE),
                    Optional.of(POTIONS)
                ));
                return;
            }
            try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack.copy())) {
                if (capability == null) {
                    return;
                }
                FluidStack fluid = capability.extractAny(81000);
                if (fluid.isEmpty()) {
                    return;
                }
                ResourceLocation itemName = BuiltInRegistries.ITEM.getKey(stack.getItem());
                ResourceLocation fluidName = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    MOD_ID,
                    "empty_" + itemName.getNamespace() + "_" + itemName.getPath() + "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()
                );
                registry.add(new DrainingDisplay(
                    EntryIngredients.of(stack),
                    IngredientHelper.createEntryIngredient(fluid),
                    EntryIngredients.of(capability.getContainer()),
                    Optional.of(id)
                ));
            }
        });
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output, result);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.DRAINING;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return location;
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
