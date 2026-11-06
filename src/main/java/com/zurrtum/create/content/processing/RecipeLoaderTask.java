package com.zurrtum.create.content.processing;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryLoadTask.PendingRegistration;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;

public record RecipeLoaderTask(
    BiFunction<Identifier, Resource, @Nullable PendingRegistration<Recipe<?>>> operation) implements BiFunction<Identifier, Resource, PendingRegistration<Recipe<?>>> {
    @Override
    public @Nullable PendingRegistration<Recipe<?>> apply(Identifier identifier, Resource resource) {
        PendingRegistration<Recipe<?>> entry = operation.apply(identifier, resource);
        if (entry != null) {
            Optional<Recipe<?>> data = entry.value().left();
            if (data.isPresent()) {
                RecipeLoader.onLoadRecipe(entry, data.get());
            }
        }
        return entry;
    }
}
