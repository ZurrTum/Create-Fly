package com.zurrtum.create;

import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.HauntingRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;

import static com.zurrtum.create.Create.MOD_ID;

public class AllRecipeSets {
    public static final Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> ALL = new IdentityHashMap<>();
    public static final ResourceKey<RecipePropertySet> ITEM_APPLICATION_TARGET = register("item_application_target");
    public static final ResourceKey<RecipePropertySet> ITEM_APPLICATION_INGREDIENT = register("item_application_ingredient");
    public static final ResourceKey<RecipePropertySet> EMPTYING = register("emptying");
    public static final ResourceKey<RecipePropertySet> FILLING = register("filling");
    public static final ResourceKey<RecipePropertySet> SAND_PAPER_POLISHING = register("sand_paper_polishing");
    public static final ResourceKey<RecipePropertySet> SPLASHING = register("splashing");
    public static final ResourceKey<RecipePropertySet> HAUNTING = register("haunting");
    public static final ResourceKey<RecipePropertySet> CRUSHING = register("crushing");
    public static final ResourceKey<RecipePropertySet> MILLING = register("milling");

    private static ResourceKey<RecipePropertySet> register(String id) {
        return ResourceKey.create(RecipePropertySet.TYPE_KEY, ResourceLocation.fromNamespaceAndPath(MOD_ID, id));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> void register(ResourceKey<RecipePropertySet> key, Class<T> type, Function<T, Ingredient> getter) {
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
