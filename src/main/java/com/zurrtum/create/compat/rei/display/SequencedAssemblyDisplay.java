package com.zurrtum.create.compat.rei.display;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SequencedAssemblyDisplay(
    EntryIngredient input, SequenceData sequences, ChanceOutput output, int loop, Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<SequencedAssemblyDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(SequencedAssemblyDisplay::input),
            SequenceData.CODEC.fieldOf("sequences").forGetter(SequencedAssemblyDisplay::sequences),
            ChanceOutput.CODEC.fieldOf("output").forGetter(SequencedAssemblyDisplay::output),
            Codec.INT.fieldOf("loop").forGetter(SequencedAssemblyDisplay::loop),
            Identifier.CODEC.optionalFieldOf("location").forGetter(SequencedAssemblyDisplay::location)
        ).apply(instance, SequencedAssemblyDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            SequencedAssemblyDisplay::input,
            SequenceData.PACKET_CODEC,
            SequencedAssemblyDisplay::sequences,
            ChanceOutput.PACKET_CODEC,
            SequencedAssemblyDisplay::output,
            PacketCodecs.INTEGER,
            SequencedAssemblyDisplay::loop,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            SequencedAssemblyDisplay::location,
            SequencedAssemblyDisplay::new
        )
    );

    public SequencedAssemblyDisplay(RecipeEntry<SequencedAssemblyRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public SequencedAssemblyDisplay(Identifier id, SequencedAssemblyRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), SequenceData.create(recipe), recipe.result(), recipe.loops(), Optional.of(id));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        List<EntryIngredient> inputs = new ArrayList<>();
        inputs.add(input);
        for (EntryIngredient ingredient : sequences.ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            inputs.add(ingredient);
        }
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(EntryIngredients.of(output.stack()));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.SEQUENCED_ASSEMBLY;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }

    public record SequenceData(List<RecipeType<?>> types, List<List<Text>> tooltip, List<EntryIngredient> ingredients) {
        public static final Codec<SequenceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.RECIPE_TYPE.getCodec().listOf().fieldOf("types").forGetter(SequenceData::types),
            TextCodecs.CODEC.listOf().listOf().fieldOf("tooltip").forGetter(SequenceData::tooltip),
            EntryIngredient.codec().listOf().fieldOf("ingredients").forGetter(SequenceData::ingredients)
        ).apply(instance, SequenceData::new));
        public static final PacketCodec<RegistryByteBuf, SequenceData> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryValue(RegistryKeys.RECIPE_TYPE).collect(PacketCodecs.toList()),
            SequenceData::types,
            TextCodecs.PACKET_CODEC.collect(PacketCodecs.toList()).collect(PacketCodecs.toList()),
            SequenceData::tooltip,
            EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
            SequenceData::ingredients,
            SequenceData::new
        );

        public static SequenceData create(SequencedAssemblyRecipe recipe) {
            ImmutableList.Builder<EntryIngredient> ingredientBuilder = ImmutableList.builder();
            ImmutableList.Builder<List<Text>> textBuilder = ImmutableList.builder();
            ImmutableList.Builder<RecipeType<?>> typeBuilder = ImmutableList.builder();
            List<Recipe<?>> recipes = recipe.sequence();
            for (int i = 0, size = recipes.size() / recipe.loops(); i < size; i++) {
                Recipe<?> sequence = recipes.get(i);
                typeBuilder.add(sequence.getType());
                ImmutableList.Builder<Text> tooltipBuilder = ImmutableList.builder();
                tooltipBuilder.add(Text.translatable("create.recipe.assembly.step", i + 1));
                if (sequence instanceof DeployerApplicationRecipe deployerApplicationRecipe) {
                    tooltipBuilder.add(Text.translatable("create.recipe.assembly.deploying_item", "").formatted(Formatting.DARK_GREEN));
                    ingredientBuilder.add(EntryIngredients.ofIngredient(deployerApplicationRecipe.ingredient()));
                } else if (sequence instanceof FillingRecipe fillingRecipe) {
                    tooltipBuilder.add(Text.translatable("create.recipe.assembly.spout_filling_fluid", "").formatted(Formatting.DARK_GREEN));
                    ingredientBuilder.add(IngredientHelper.createEntryIngredient(fillingRecipe.fluidIngredient()));
                } else {
                    ingredientBuilder.add(EntryIngredient.empty());
                    Identifier id = Registries.RECIPE_TYPE.getId(sequence.getType());
                    if (id != null) {
                        String namespace = id.getNamespace();
                        String recipeName;
                        if (namespace.equals("create")) {
                            recipeName = id.getPath();
                        } else {
                            recipeName = id.getNamespace() + "." + id.getPath();
                        }
                        tooltipBuilder.add(Text.translatable("create.recipe.assembly." + recipeName).formatted(Formatting.DARK_GREEN));
                    } else {
                        tooltipBuilder.add(ScreenTexts.EMPTY);
                    }
                }
                textBuilder.add(tooltipBuilder.build());
            }
            return new SequenceData(typeBuilder.build(), textBuilder.build(), ingredientBuilder.build());
        }
    }
}
