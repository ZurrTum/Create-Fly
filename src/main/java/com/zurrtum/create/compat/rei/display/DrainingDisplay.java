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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

public record DrainingDisplay(
    EntryIngredient input, EntryIngredient output, EntryIngredient result, Optional<Identifier> location
) implements Display {
    public static final Identifier POTIONS = Identifier.of(MOD_ID, "potions");
    public static final DisplaySerializer<DrainingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(DrainingDisplay::input),
            EntryIngredient.codec().fieldOf("output").forGetter(DrainingDisplay::output),
            EntryIngredient.codec().fieldOf("result").forGetter(DrainingDisplay::result),
            Identifier.CODEC.optionalFieldOf("location").forGetter(DrainingDisplay::location)
        ).apply(instance, DrainingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            DrainingDisplay::input,
            EntryIngredient.streamCodec(),
            DrainingDisplay::output,
            EntryIngredient.streamCodec(),
            DrainingDisplay::result,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            DrainingDisplay::location,
            DrainingDisplay::new
        )
    );

    public DrainingDisplay(RecipeEntry<EmptyingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public DrainingDisplay(Identifier id, EmptyingRecipe recipe) {
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
                Identifier itemName = Registries.ITEM.getId(stack.getItem());
                Identifier fluidName = Registries.FLUID.getId(fluid.getFluid());
                Identifier id = Identifier.of(
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
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
