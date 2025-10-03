package com.zurrtum.create;

import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.HauntingRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.zurrtum.create.Create.MOD_ID;

public class AllRecipeTypes {
    public static final List<RecipeType<? extends ItemApplicationRecipe>> DEPLOYER_RECIPES = new ArrayList<>();
    public static final RecipeType<CrushingRecipe> CRUSHING = register("crushing");
    public static final RecipeType<CuttingRecipe> CUTTING = register("cutting");
    public static final RecipeType<MillingRecipe> MILLING = register("milling");
    public static final RecipeType<MixingRecipe> MIXING = register("mixing");
    public static final RecipeType<CompactingRecipe> COMPACTING = register("compacting");
    public static final RecipeType<PressingRecipe> PRESSING = register("pressing");
    public static final RecipeType<SandPaperPolishingRecipe> SANDPAPER_POLISHING = register("sandpaper_polishing");
    public static final RecipeType<SplashingRecipe> SPLASHING = register("splashing");
    public static final RecipeType<HauntingRecipe> HAUNTING = register("haunting");
    public static final RecipeType<DeployerApplicationRecipe> DEPLOYING = register("deploying");
    public static final RecipeType<FillingRecipe> FILLING = register("filling");
    public static final RecipeType<EmptyingRecipe> EMPTYING = register("emptying");
    public static final RecipeType<ManualApplicationRecipe> ITEM_APPLICATION = register("item_application");
    public static final RecipeType<MechanicalCraftingRecipe> MECHANICAL_CRAFTING = register("mechanical_crafting");
    public static final RecipeType<SequencedAssemblyRecipe> SEQUENCED_ASSEMBLY = register("sequenced_assembly");
    public static final RecipeType<PotionRecipe> POTION = register("potion");

    private static final TagKey<RecipeSerializer<?>> AUTOMATION_IGNORE_TAG = TagKey.of(
        RegistryKeys.RECIPE_SERIALIZER,
        Identifier.of(MOD_ID, "automation_ignore")
    );
    public static final Predicate<RecipeEntry<?>> CAN_BE_AUTOMATED = r -> !r.id().getValue().getPath().endsWith("_manual_only");

    public static boolean shouldIgnoreInAutomation(RecipeEntry<?> recipe) {
        RecipeSerializer<?> serializer = recipe.value().getSerializer();
        if (serializer != null && Registries.RECIPE_SERIALIZER.getEntry(serializer).isIn(AllRecipeTypes.AUTOMATION_IGNORE_TAG))
            return true;
        return !CAN_BE_AUTOMATED.test(recipe);
    }

    private static <T extends Recipe<?>> RecipeType<T> register(String name) {
        Identifier id = Identifier.of(MOD_ID, name);
        return Registry.register(
            Registries.RECIPE_TYPE, id, new RecipeType<T>() {
                public String toString() {
                    return id.toString();
                }
            }
        );
    }

    public static void register() {
        DEPLOYER_RECIPES.add(AllRecipeTypes.DEPLOYING);
        DEPLOYER_RECIPES.add(AllRecipeTypes.ITEM_APPLICATION);
    }
}