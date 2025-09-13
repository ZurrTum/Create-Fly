package com.zurrtum.create;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class AllAssemblyRecipeNames {
    private static final Map<String, BiFunction<DynamicOps<JsonElement>, JsonObject, Text>> ALL = new HashMap<>();

    public static Text get(DynamicOps<JsonElement> ops, JsonObject json) {
        String type = json.get("type").getAsString();
        BiFunction<DynamicOps<JsonElement>, JsonObject, Text> factory = ALL.get(type);
        if (factory != null) {
            return factory.apply(ops, json);
        }
        String name;
        if (type.startsWith("create:")) {
            name = type.replaceFirst("create:", "");
        } else {
            name = type.replaceFirst(":", ".");
        }
        return Text.translatable("create.recipe.assembly." + name);
    }

    public static void register(RecipeType<?> id, BiFunction<DynamicOps<JsonElement>, JsonObject, Text> factory) {
        ALL.put(id.toString(), factory);
    }

    public static void register() {
        register(AllRecipeTypes.DEPLOYING, DeployerApplicationRecipe::getDescriptionForAssembly);
        register(AllRecipeTypes.FILLING, FillingRecipe::getDescriptionForAssembly);
    }
}
