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
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

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
        ).apply(instance, SequencedAssemblyDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            SequencedAssemblyDisplay::input,
            SequenceData.PACKET_CODEC,
            SequencedAssemblyDisplay::sequences,
            ChanceOutput.PACKET_CODEC,
            SequencedAssemblyDisplay::output,
            ByteBufCodecs.INT,
            SequencedAssemblyDisplay::loop,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            SequencedAssemblyDisplay::location,
            SequencedAssemblyDisplay::new
        )
    );

    public SequencedAssemblyDisplay(RecipeHolder<SequencedAssemblyRecipe> entry) {
        this(entry.id().location(), entry.value());
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

    public record SequenceData(List<RecipeType<?>> types, List<List<Component>> tooltip, List<EntryIngredient> ingredients) {
        public static final Codec<SequenceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.RECIPE_TYPE.byNameCodec().listOf().fieldOf("types").forGetter(SequenceData::types),
            ComponentSerialization.CODEC.listOf().listOf().fieldOf("tooltip").forGetter(SequenceData::tooltip),
            EntryIngredient.codec().listOf().fieldOf("ingredients").forGetter(SequenceData::ingredients)
        ).apply(instance, SequenceData::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SequenceData> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.RECIPE_TYPE).apply(ByteBufCodecs.list()),
            SequenceData::types,
            ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()),
            SequenceData::tooltip,
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
            SequenceData::ingredients,
            SequenceData::new
        );

        public static SequenceData create(SequencedAssemblyRecipe recipe) {
            ImmutableList.Builder<EntryIngredient> ingredientBuilder = ImmutableList.builder();
            ImmutableList.Builder<List<Component>> textBuilder = ImmutableList.builder();
            ImmutableList.Builder<RecipeType<?>> typeBuilder = ImmutableList.builder();
            List<Recipe<?>> recipes = recipe.sequence();
            for (int i = 0, size = recipes.size() / recipe.loops(); i < size; i++) {
                Recipe<?> sequence = recipes.get(i);
                typeBuilder.add(sequence.getType());
                ImmutableList.Builder<Component> tooltipBuilder = ImmutableList.builder();
                tooltipBuilder.add(Component.translatable("create.recipe.assembly.step", i + 1));
                if (sequence instanceof DeployerApplicationRecipe deployerApplicationRecipe) {
                    tooltipBuilder.add(Component.translatable("create.recipe.assembly.deploying_item", "").withStyle(ChatFormatting.DARK_GREEN));
                    ingredientBuilder.add(EntryIngredients.ofIngredient(deployerApplicationRecipe.ingredient()));
                } else if (sequence instanceof FillingRecipe fillingRecipe) {
                    tooltipBuilder.add(Component.translatable("create.recipe.assembly.spout_filling_fluid", "").withStyle(ChatFormatting.DARK_GREEN));
                    ingredientBuilder.add(IngredientHelper.createEntryIngredient(fillingRecipe.fluidIngredient()));
                } else {
                    ingredientBuilder.add(EntryIngredient.empty());
                    Identifier id = BuiltInRegistries.RECIPE_TYPE.getKey(sequence.getType());
                    if (id != null) {
                        String namespace = id.getNamespace();
                        String recipeName;
                        if (namespace.equals("create")) {
                            recipeName = id.getPath();
                        } else {
                            recipeName = id.getNamespace() + "." + id.getPath();
                        }
                        tooltipBuilder.add(Component.translatable("create.recipe.assembly." + recipeName).withStyle(ChatFormatting.DARK_GREEN));
                    } else {
                        tooltipBuilder.add(CommonComponents.EMPTY);
                    }
                }
                textBuilder.add(tooltipBuilder.build());
            }
            return new SequenceData(typeBuilder.build(), textBuilder.build(), ingredientBuilder.build());
        }
    }
}
