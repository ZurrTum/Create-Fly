package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.zurrtum.create.Create.MOD_ID;

public record SpoutFillingDisplay(
    EntryIngredient input, EntryIngredient fluid, EntryIngredient output, Optional<Identifier> location
) implements Display {
    public static final Identifier POTIONS = Identifier.of(MOD_ID, "potions");
    public static final DisplaySerializer<SpoutFillingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(SpoutFillingDisplay::input),
            EntryIngredient.codec().fieldOf("fluid").forGetter(SpoutFillingDisplay::fluid),
            EntryIngredient.codec().fieldOf("output").forGetter(SpoutFillingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(SpoutFillingDisplay::location)
        ).apply(instance, SpoutFillingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            SpoutFillingDisplay::input,
            EntryIngredient.streamCodec(),
            SpoutFillingDisplay::fluid,
            EntryIngredient.streamCodec(),
            SpoutFillingDisplay::output,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            SpoutFillingDisplay::location,
            SpoutFillingDisplay::new
        )
    );

    public SpoutFillingDisplay(RecipeEntry<FillingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public SpoutFillingDisplay(Identifier id, FillingRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            IngredientHelper.createEntryIngredient(recipe.fluidIngredient()),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    public static void register(ServerDisplayRegistry registry) {
        List<FluidStack> fluids = EntryRegistry.getInstance().getEntryStacks()
            .filter(stack -> Objects.equals(stack.getType(), VanillaEntryTypes.FLUID)).map(entry -> {
                dev.architectury.fluid.FluidStack stack = entry.castValue();
                return new FluidStack(stack.getFluid(), stack.getAmount(), stack.getPatch());
            }).toList();
        EntryRegistry.getInstance().getEntryStacks().filter(stack -> Objects.equals(stack.getType(), VanillaEntryTypes.ITEM))
            .<EntryStack<ItemStack>>map(EntryStack::cast).forEach(entry -> {
                ItemStack stack = entry.getValue();
                if (PotionFluidHandler.isPotionItem(stack)) {
                    //TODO
                    //                    registry.add(new SpoutFillingDisplay(
                    //                        EntryIngredients.of(Items.GLASS_BOTTLE),
                    //                        IngredientHelper.createEntryIngredient(PotionFluidHandler.getFluidFromPotionItem(stack)),
                    //                        EntryIngredients.of(stack),
                    //                        Optional.of(POTIONS)
                    //                    ));
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
                            if (!result.isOf(item)) {
                                Identifier itemName = Registries.ITEM.getId(item);
                                Identifier fluidName = Registries.FLUID.getId(fluid.getFluid());
                                Identifier id = Identifier.of(
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
