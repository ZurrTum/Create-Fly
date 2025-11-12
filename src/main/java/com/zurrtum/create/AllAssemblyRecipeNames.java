package com.zurrtum.create;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;

public class AllAssemblyRecipeNames {
    private static final Map<String, BiFunction<DynamicOps<JsonElement>, JsonObject, Component>> ALL = new HashMap<>();

    public static Component get(DynamicOps<JsonElement> ops, JsonObject json) {
        String type = json.get("type").getAsString();
        BiFunction<DynamicOps<JsonElement>, JsonObject, Component> factory = ALL.get(type);
        if (factory != null) {
            return factory.apply(ops, json);
        }
        String name;
        if (type.startsWith("create:")) {
            name = type.replaceFirst("create:", "");
        } else {
            name = type.replaceFirst(":", ".");
        }
        return Component.translatable("create.recipe.assembly." + name);
    }

    public static void register(RecipeType<?> id, BiFunction<DynamicOps<JsonElement>, JsonObject, Component> factory) {
        ALL.put(id.toString(), factory);
    }

    public static void register() {
        register(AllRecipeTypes.DEPLOYING, DeployerApplicationRecipe::getDescriptionForAssembly);
        register(AllRecipeTypes.FILLING, FillingRecipe::getDescriptionForAssembly);
    }
}
