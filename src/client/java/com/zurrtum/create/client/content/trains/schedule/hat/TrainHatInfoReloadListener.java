package com.zurrtum.create.client.content.trains.schedule.hat;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.Create;
import com.zurrtum.create.foundation.utility.CreateResourceReloader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.phys.Vec3;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

public class TrainHatInfoReloadListener {

    private static final Map<EntityType<?>, TrainHatInfo> ENTITY_INFO_MAP = new HashMap<>();
    public static final String HAT_INFO_DIRECTORY = "train_hat_info";
    public static final ResourceManagerReloadListener LISTENER = new ReloadListener();
    public static final TrainHatInfo DEFAULT = new TrainHatInfo("", 0, Vec3.ZERO, 1.0F);

    public static TrainHatInfo getHatInfoFor(Entity entity) {
        // Manual override for snow golems, they are a special case when they have a pumpkin on their head
        if (entity instanceof SnowGolem snowGolem && snowGolem.hasPumpkin()) {
            return new TrainHatInfo("", 0, new Vec3(0.0F, -3.0F, 0.0F), 1.18F);
        }

        return ENTITY_INFO_MAP.getOrDefault(entity.getType(), DEFAULT);
    }

    private static class ReloadListener extends CreateResourceReloader {
        public ReloadListener() {
            super("hat");
        }

        @Override
        public void onResourceManagerReload(ResourceManager manager) {
            ENTITY_INFO_MAP.clear();

            FileToIdConverter converter = FileToIdConverter.json(HAT_INFO_DIRECTORY);
            converter.listMatchingResources(manager).forEach((location, resource) -> {
                String[] splitPath = location.getPath().split("/");
                Identifier entityName = Identifier.fromNamespaceAndPath(
                    location.getNamespace(),
                    splitPath[splitPath.length - 1].replace(".json", "")
                );
                if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityName)) {
                    Create.LOGGER.error("Failed to load train hat info for entity {} as it does not exist.", entityName);
                    return;
                }

                try (BufferedReader reader = resource.openAsReader()) {
                    JsonObject json = GsonHelper.parse(reader);
                    ENTITY_INFO_MAP.put(
                        BuiltInRegistries.ENTITY_TYPE.getValue(entityName),
                        TrainHatInfo.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(Create.LOGGER::error).orElseThrow()
                    );
                } catch (Exception e) {
                    Create.LOGGER.error("Failed to read train hat info for entity {}!", entityName, e);
                }
            });
            Create.LOGGER.info("Loaded {} train hat configurations.", ENTITY_INFO_MAP.size());
        }
    }
}
