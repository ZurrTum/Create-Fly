package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

public record SpoutFillingDisplay(
    EntryIngredient input, EntryIngredient fluid, EntryIngredient output, Optional<Identifier> location
) implements Display {
    public static final Identifier POTIONS = Identifier.fromNamespaceAndPath(MOD_ID, "potions");
    public static final DisplaySerializer<SpoutFillingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(SpoutFillingDisplay::input),
            EntryIngredient.codec().fieldOf("fluid").forGetter(SpoutFillingDisplay::fluid),
            EntryIngredient.codec().fieldOf("output").forGetter(SpoutFillingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(SpoutFillingDisplay::location)
        ).apply(instance, SpoutFillingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            SpoutFillingDisplay::input,
            EntryIngredient.streamCodec(),
            SpoutFillingDisplay::fluid,
            EntryIngredient.streamCodec(),
            SpoutFillingDisplay::output,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            SpoutFillingDisplay::location,
            SpoutFillingDisplay::new
        )
    );

    public SpoutFillingDisplay(RecipeHolder<FillingRecipe> entry) {
        this(entry.id().identifier(), entry.value());
    }

    public SpoutFillingDisplay(Identifier id, FillingRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            IngredientHelper.createEntryIngredient(recipe.fluidIngredient()),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    public static void register(Stream<EntryStack<?>> itemStream, Stream<EntryStack<?>> fluidStream, DisplayConsumer registry) {
        List<FluidStack> fluids = fluidStream.map(entry -> {
            dev.architectury.fluid.FluidStack stack = entry.castValue();
            return new FluidStack(stack.getFluid(), stack.getAmount(), stack.getComponents().asPatch());
        }).toList();
        itemStream.forEach(entry -> {
            ItemStack stack = entry.castValue();
            if (PotionFluidHandler.isPotionItem(stack)) {
                registry.add(new SpoutFillingDisplay(
                    EntryIngredients.of(Items.GLASS_BOTTLE),
                    IngredientHelper.createEntryIngredient(PotionFluidHandler.getFluidFromPotionItem(stack)),
                    EntryIngredients.of(stack),
                    Optional.of(POTIONS)
                ));
                return;
            }
            try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack.copy())) {
                if (capability == null) {
                    return;
                }
                int size = capability.size();
                FluidStack existingFluid = size == 1 ? capability.getStack(0) : FluidStack.EMPTY;
                for (FluidStack fluid : fluids) {
                    if (size == 1 && !existingFluid.isEmpty() && !FluidStack.areFluidsAndComponentsEqual(existingFluid, fluid)) {
                        continue;
                    }
                    int insert = capability.insert(fluid, 81000);
                    if (insert == 0) {
                        continue;
                    }
                    ItemStack result = capability.getContainer();
                    if (!result.isEmpty()) {
                        Item item = stack.getItem();
                        if (!result.is(item)) {
                            Identifier itemName = BuiltInRegistries.ITEM.getKey(item);
                            Identifier fluidName = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
                            Identifier id = Identifier.fromNamespaceAndPath(
                                MOD_ID,
                                "fill_" + itemName.getNamespace() + "_" + itemName.getPath() + "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()
                            );
                            registry.add(new SpoutFillingDisplay(
                                EntryIngredients.of(stack),
                                EntryIngredients.of(dev.architectury.fluid.FluidStack.create(fluid.getFluid(), insert, fluid.getComponentChanges())),
                                EntryIngredients.of(result),
                                Optional.of(id)
                            ));
                        }
                    }
                    capability.extract(fluid, insert);
                }
            }
        });
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input, fluid);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.SPOUT_FILLING;
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
