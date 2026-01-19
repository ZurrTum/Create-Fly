package com.zurrtum.create.foundation.pack;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.foundation.data.recipe.Mods;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zurrtum.create.Create.MOD_ID;

public class RuntimeDataGenerator {
    private static final Set<String> IGNORES = Set.of(Identifier.DEFAULT_NAMESPACE, MOD_ID);
    // (1. variant_prefix, optional, can be null)stripped_(2. wood name)(3. type)(4. empty group)endofline
    private static final Pattern STRIPPED_WOODS_PREFIX_REGEX = Pattern.compile(
        "(\\w*)??stripped_(\\w*)(_log|_wood|_stem|_hyphae|_block|(?<!_)wood)()$");
    // (1. wood name)(2. type)(3. variant_suffix, optional)_stripped(4. 2nd variant_suffix, optional)
    private static final Pattern STRIPPED_WOOD_SUFFIX_REGEX = Pattern.compile(
        "(\\w*)(_log|_wood|_stem|_hyphae|_block|(?<!_)wood)(\\w*)_stripped(\\w*)");
    // startofline(not preceded by stripped_)(1. wood_name)(2. type)(3. (4. variant suffix), optional, that doesn't end in _stripped, can be null)endofline
    private static final Pattern NON_STRIPPED_WOODS_REGEX = Pattern.compile(
        "^(?!stripped_)([a-z_]+)(_log|_wood|_stem|_hyphae|(?<!bioshroom)_block)(([a-z_]+)(?<!_stripped))?$");
    private static final Multimap<Identifier, TagEntry> TAGS = HashMultimap.create();
    private static final Object2ObjectOpenHashMap<Identifier, JsonElement> JSON_FILES = new Object2ObjectOpenHashMap<>();
    private static final Map<Identifier, Identifier> MISMATCHED_WOOD_NAMES = ImmutableMap.<Identifier, Identifier>builder()
        .put(Mods.ARS_N.asResource("blue_archwood"), Mods.ARS_N.asResource("archwood")) // Generate recipes for planks -> everything else
        //.put(Mods.UUE.asResource("chorus_cane"), Mods.UUE.asResource("chorus_nest")) // Has a weird setup with both normal and stripped planks, that it already provides cutting recipes for
        .put(Mods.DD.asResource("blooming"), Mods.DD.asResource("bloom")).build();

    public static void insertIntoPack(DynamicPack dynamicPack) {
        Identifier cuttingId = BuiltInRegistries.RECIPE_TYPE.getKey(AllRecipeTypes.CUTTING);
        Identifier splashingId = BuiltInRegistries.RECIPE_TYPE.getKey(AllRecipeTypes.SPLASHING);
        assert cuttingId != null;
        assert splashingId != null;
        for (Identifier itemId : BuiltInRegistries.ITEM.keySet()) {
            if (IGNORES.contains(itemId.getNamespace())) {
                continue;
            }
            cuttingRecipes(cuttingId, itemId);
            washingRecipes(splashingId, itemId);
        }

        if (!JSON_FILES.isEmpty()) {
            Create.LOGGER.info("Created {} recipes which will be injected into the game", JSON_FILES.size());
            JSON_FILES.forEach(dynamicPack::put);
            JSON_FILES.clear();
            JSON_FILES.trim();
        }

        if (!TAGS.isEmpty()) {
            Create.LOGGER.info("Created {} tags which will be injected into the game", TAGS.size());
            for (Map.Entry<Identifier, Collection<TagEntry>> tags : TAGS.asMap().entrySet()) {
                TagFile tagFile = new TagFile(new ArrayList<>(tags.getValue()), false);
                dynamicPack.put(tags.getKey().withPrefix("tags/item/"), TagFile.CODEC.encodeStart(JsonOps.INSTANCE, tagFile).result().orElseThrow());
            }
            TAGS.clear();
        }
    }

    // logs/woods -> stripped variants
    // logs/woods both stripped and non stripped -> planks
    // planks -> stairs, slabs, fences, fence gates, doors, trapdoors, pressure plates, buttons and signs
    private static void cuttingRecipes(Identifier typeId, Identifier itemId) {
        String path = itemId.getPath();

        Matcher match = STRIPPED_WOODS_PREFIX_REGEX.matcher(path);
        boolean hasFoundMatch = match.find();
        boolean strippedInPrefix = hasFoundMatch;

        if (!hasFoundMatch) {
            match = STRIPPED_WOOD_SUFFIX_REGEX.matcher(path);
            hasFoundMatch = match.find();
        }

        // Last ditch attempt. Try to find logs without stripped variants
        boolean noStrippedVariant = false;
        if (!hasFoundMatch && !BuiltInRegistries.ITEM.containsKey(itemId.withPrefix("stripped_")) && !BuiltInRegistries.ITEM.containsKey(itemId.withSuffix(
            "_stripped"))) {
            match = NON_STRIPPED_WOODS_REGEX.matcher(path);
            hasFoundMatch = match.find();
            noStrippedVariant = true;
        }

        if (hasFoundMatch) {
            String prefix = strippedInPrefix && match.group(1) != null ? match.group(1) : "";
            String suffix = !strippedInPrefix && !noStrippedVariant ? match.group(3) + match.group(4) : "";
            String type = match.group(strippedInPrefix ? 3 : 2);
            Identifier matched_name = itemId.withPath(match.group(strippedInPrefix ? 2 : 1));
            // re-add 'wood' to wood types such as Botania's livingwood
            Identifier base = matched_name.withSuffix(type.equals("wood") ? "wood" : "");
            base = MISMATCHED_WOOD_NAMES.getOrDefault(base, base);
            Identifier nonStrippedId = matched_name.withSuffix(type).withPrefix(prefix).withSuffix(suffix);
            Identifier planksId = base.withSuffix("_planks");
            Identifier stairsId = base.withSuffix(base.getNamespace().equals(Mods.BTN.getId()) ? "_planks_stairs" : "_stairs");
            Identifier slabId = base.withSuffix(base.getNamespace().equals(Mods.BTN.getId()) ? "_planks_slab" : "_slab");
            Identifier fenceId = base.withSuffix("_fence");
            Identifier fenceGateId = base.withSuffix("_fence_gate");
            Identifier doorId = base.withSuffix("_door");
            Identifier trapdoorId = base.withSuffix("_trapdoor");
            Identifier pressurePlateId = base.withSuffix("_pressure_plate");
            Identifier buttonId = base.withSuffix("_button");
            Identifier signId = base.withSuffix("_sign");
            // Bamboo, GotD whistlecane
            int planksCount = type.contains("block") ? 3 : 6;

            if (!noStrippedVariant) {
                // Catch mods like JNE that have a non-stripped log prefixed but not the stripped log
                if (BuiltInRegistries.ITEM.containsKey(nonStrippedId)) {
                    simpleWoodRecipe(typeId, nonStrippedId, itemId);
                }
                simpleWoodRecipe(typeId, itemId, planksId, planksCount);
            } else if (BuiltInRegistries.ITEM.containsKey(planksId)) {
                Identifier tag = Identifier.fromNamespaceAndPath(MOD_ID, "runtime_generated/compat/" + itemId.getNamespace() + "/" + base.getPath());
                insertIntoTag(tag, itemId);

                simpleWoodRecipe(typeId, TagKey.create(Registries.ITEM, tag), planksId, planksCount);
            }

            if (!path.contains("_wood") && !path.contains("_hyphae") && BuiltInRegistries.ITEM.containsKey(planksId)) {
                simpleWoodRecipe(typeId, planksId, stairsId);
                simpleWoodRecipe(typeId, planksId, slabId, 2);
                simpleWoodRecipe(typeId, planksId, fenceId);
                simpleWoodRecipe(typeId, planksId, fenceGateId);
                simpleWoodRecipe(typeId, planksId, doorId);
                simpleWoodRecipe(typeId, planksId, trapdoorId);
                simpleWoodRecipe(typeId, planksId, pressurePlateId);
                simpleWoodRecipe(typeId, planksId, buttonId);
                simpleWoodRecipe(typeId, planksId, signId);
            }
        }
    }

    private static void washingRecipes(Identifier typeId, Identifier itemId) {
        Block block = BuiltInRegistries.BLOCK.getValue(itemId);
        if (block instanceof ConcretePowderBlock concretePowderBlock) {
            simpleSplashingRecipe(typeId, itemId, BuiltInRegistries.BLOCK.getKey(concretePowderBlock.concrete));
        }
    }

    private static void insertIntoTag(Identifier tag, Identifier itemId) {
        if (BuiltInRegistries.ITEM.containsKey(itemId))
            TAGS.put(tag, TagEntry.optionalElement(itemId));
    }

    private static void simpleWoodRecipe(Identifier typeId, Identifier inputId, Identifier outputId) {
        simpleWoodRecipe(typeId, inputId, outputId, 1);
    }

    private static void simpleWoodRecipe(Identifier typeId, Identifier inputId, Identifier outputId, int amount) {
        if (BuiltInRegistries.ITEM.containsKey(outputId)) {
            addRecipe(
                typeId, inputId.getNamespace(), inputId.getPath(), outputId.getPath(), new CuttingRecipe(
                    50,
                    new ItemStackTemplate(BuiltInRegistries.ITEM.getValue(outputId), amount),
                    Ingredient.of(BuiltInRegistries.ITEM.getValue(inputId))
                )
            );
        }
    }

    private static void simpleWoodRecipe(Identifier typeId, TagKey<Item> inputTag, Identifier outputId, int amount) {
        if (BuiltInRegistries.ITEM.containsKey(outputId)) {
            Recipe.CODEC.encodeStart(
                EmptyJsonOps.INSTANCE,
                new CuttingRecipe(50, new ItemStackTemplate(BuiltInRegistries.ITEM.getValue(outputId), amount), EmptyJsonOps.ofTag(inputTag))
            ).ifSuccess(json -> {
                Identifier inputId = inputTag.location();
                Identifier path = Identifier.fromNamespaceAndPath(
                    typeId.getNamespace(),
                    "recipe/" + typeId.getPath() + "/runtime_generated/compat/" + inputId.getNamespace() + "/" + "tag_" + inputId.getPath() + "_to_" + outputId.getPath()
                );
                JSON_FILES.put(path, json);
            });
        }
    }

    private static void simpleSplashingRecipe(Identifier typeId, Identifier first, Identifier second) {
        addRecipe(
            typeId,
            first.getNamespace(),
            first.getPath(),
            second.getPath(),
            new SplashingRecipe(
                List.of(new ChanceOutput(1, new ItemStackTemplate(BuiltInRegistries.BLOCK.getValue(second).asItem()))),
                Ingredient.of(BuiltInRegistries.BLOCK.getValue(first))
            )
        );
    }

    private static void addRecipe(Identifier typeId, String modid, String from, String to, Recipe<?> recipe) {
        Recipe.CODEC.encodeStart(JsonOps.INSTANCE, recipe).ifSuccess(json -> {
            Identifier path = Identifier.fromNamespaceAndPath(
                typeId.getNamespace(),
                "recipe/" + typeId.getPath() + "/runtime_generated/compat/" + modid + "/" + from + "_to_" + to
            );
            JSON_FILES.put(path, json);
        });
    }
}