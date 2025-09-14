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
import net.minecraft.block.Block;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagFile;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

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
        Identifier cuttingId = Registries.RECIPE_TYPE.getId(AllRecipeTypes.CUTTING);
        Identifier splashingId = Registries.RECIPE_TYPE.getId(AllRecipeTypes.SPLASHING);
        for (Identifier itemId : Registries.ITEM.getIds()) {
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
                dynamicPack.put(
                    tags.getKey().withPrefixedPath("tags/item/"),
                    TagFile.CODEC.encodeStart(JsonOps.INSTANCE, tagFile).result().orElseThrow()
                );
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
        if (!hasFoundMatch && !Registries.ITEM.containsId(itemId.withPrefixedPath("stripped_")) && !Registries.ITEM.containsId(itemId.withSuffixedPath(
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
            Identifier base = matched_name.withSuffixedPath(type.equals("wood") ? "wood" : "");
            base = MISMATCHED_WOOD_NAMES.getOrDefault(base, base);
            Identifier nonStrippedId = matched_name.withSuffixedPath(type).withPrefixedPath(prefix).withSuffixedPath(suffix);
            Identifier planksId = base.withSuffixedPath("_planks");
            Identifier stairsId = base.withSuffixedPath(base.getNamespace().equals(Mods.BTN.getId()) ? "_planks_stairs" : "_stairs");
            Identifier slabId = base.withSuffixedPath(base.getNamespace().equals(Mods.BTN.getId()) ? "_planks_slab" : "_slab");
            Identifier fenceId = base.withSuffixedPath("_fence");
            Identifier fenceGateId = base.withSuffixedPath("_fence_gate");
            Identifier doorId = base.withSuffixedPath("_door");
            Identifier trapdoorId = base.withSuffixedPath("_trapdoor");
            Identifier pressurePlateId = base.withSuffixedPath("_pressure_plate");
            Identifier buttonId = base.withSuffixedPath("_button");
            Identifier signId = base.withSuffixedPath("_sign");
            // Bamboo, GotD whistlecane
            int planksCount = type.contains("block") ? 3 : 6;

            if (!noStrippedVariant) {
                // Catch mods like JNE that have a non-stripped log prefixed but not the stripped log
                if (Registries.ITEM.containsId(nonStrippedId)) {
                    simpleWoodRecipe(typeId, nonStrippedId, itemId);
                }
                simpleWoodRecipe(typeId, itemId, planksId, planksCount);
            } else if (Registries.ITEM.containsId(planksId)) {
                Identifier tag = Identifier.of(MOD_ID, "runtime_generated/compat/" + itemId.getNamespace() + "/" + base.getPath());
                insertIntoTag(tag, itemId);

                simpleWoodRecipe(typeId, TagKey.of(RegistryKeys.ITEM, tag), planksId, planksCount);
            }

            if (!path.contains("_wood") && !path.contains("_hyphae") && Registries.ITEM.containsId(planksId)) {
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
        Block block = Registries.BLOCK.get(itemId);
        if (block instanceof ConcretePowderBlock concretePowderBlock) {
            simpleSplashingRecipe(typeId, itemId, Registries.BLOCK.getId(concretePowderBlock.hardenedState));
        }
    }

    private static void insertIntoTag(Identifier tag, Identifier itemId) {
        if (Registries.ITEM.containsId(itemId))
            TAGS.put(tag, TagEntry.createOptional(itemId));
    }

    private static void simpleWoodRecipe(Identifier typeId, Identifier inputId, Identifier outputId) {
        simpleWoodRecipe(typeId, inputId, outputId, 1);
    }

    private static void simpleWoodRecipe(Identifier typeId, Identifier inputId, Identifier outputId, int amount) {
        if (Registries.ITEM.containsId(outputId)) {
            addRecipe(
                typeId,
                inputId.getNamespace(),
                inputId.getPath(),
                outputId.getPath(),
                new CuttingRecipe(50, new ItemStack(Registries.ITEM.get(outputId), amount), Ingredient.ofItem(Registries.ITEM.get(inputId)))
            );
        }
    }

    private static void simpleWoodRecipe(Identifier typeId, TagKey<Item> inputTag, Identifier outputId, int amount) {
        if (Registries.ITEM.containsId(outputId)) {
            Recipe.CODEC.encodeStart(
                EmptyJsonOps.INSTANCE,
                new CuttingRecipe(50, new ItemStack(Registries.ITEM.get(outputId), amount), EmptyJsonOps.ofTag(inputTag))
            ).ifSuccess(json -> {
                Identifier inputId = inputTag.id();
                Identifier path = Identifier.of(
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
                List.of(new ChanceOutput(1, new ItemStack(Registries.BLOCK.get(second)))),
                Ingredient.ofItem(Registries.BLOCK.get(first))
            )
        );
    }

    private static void addRecipe(Identifier typeId, String modid, String from, String to, Recipe<?> recipe) {
        Recipe.CODEC.encodeStart(JsonOps.INSTANCE, recipe).ifSuccess(json -> {
            Identifier path = Identifier.of(
                typeId.getNamespace(),
                "recipe/" + typeId.getPath() + "/runtime_generated/compat/" + modid + "/" + from + "_to_" + to
            );
            JSON_FILES.put(path, json);
        });
    }
}