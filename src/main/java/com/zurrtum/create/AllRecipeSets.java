package com.zurrtum.create;

import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.HauntingRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

public class AllRecipeSets {
    public static final Map<RegistryKey<RecipePropertySet>, ServerRecipeManager.SoleIngredientGetter> ALL = new IdentityHashMap<>();
    public static final RegistryKey<RecipePropertySet> ITEM_APPLICATION_TARGET = register("item_application_target");
    public static final RegistryKey<RecipePropertySet> ITEM_APPLICATION_INGREDIENT = register("item_application_ingredient");
    public static final RegistryKey<RecipePropertySet> EMPTYING = register("emptying");
    public static final RegistryKey<RecipePropertySet> FILLING = register("filling");
    public static final RegistryKey<RecipePropertySet> SAND_PAPER_POLISHING = register("sand_paper_polishing");
    public static final RegistryKey<RecipePropertySet> SPLASHING = register("splashing");
    public static final RegistryKey<RecipePropertySet> HAUNTING = register("haunting");
    public static final RegistryKey<RecipePropertySet> CRUSHING = register("crushing");
    public static final RegistryKey<RecipePropertySet> MILLING = register("milling");

    private static RegistryKey<RecipePropertySet> register(String id) {
        return RegistryKey.of(RecipePropertySet.REGISTRY, Identifier.of(MOD_ID, id));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> void register(RegistryKey<RecipePropertySet> key, Class<T> type, Function<T, Ingredient> getter) {
        ALL.put(
            key, recipe -> {
                if (type.isInstance(recipe)) {
                    return Optional.of(getter.apply((T) recipe));
                } else {
                    return Optional.empty();
                }
            }
        );
    }

    public static void register() {
        register(ITEM_APPLICATION_TARGET, ManualApplicationRecipe.class, ManualApplicationRecipe::target);
        register(ITEM_APPLICATION_INGREDIENT, ManualApplicationRecipe.class, ManualApplicationRecipe::ingredient);
        register(EMPTYING, EmptyingRecipe.class, EmptyingRecipe::ingredient);
        register(FILLING, FillingRecipe.class, FillingRecipe::ingredient);
        register(SAND_PAPER_POLISHING, SandPaperPolishingRecipe.class, SandPaperPolishingRecipe::ingredient);
        register(SPLASHING, SplashingRecipe.class, SplashingRecipe::ingredient);
        register(HAUNTING, HauntingRecipe.class, HauntingRecipe::ingredient);
        register(CRUSHING, CrushingRecipe.class, CrushingRecipe::ingredient);
        register(MILLING, MillingRecipe.class, MillingRecipe::ingredient);
    }
}
