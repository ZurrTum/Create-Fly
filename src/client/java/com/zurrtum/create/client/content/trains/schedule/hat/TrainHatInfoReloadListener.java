package com.zurrtum.create.client.content.trains.schedule.hat;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.Create;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

public class TrainHatInfoReloadListener {

    private static final Map<EntityType<?>, TrainHatInfo> ENTITY_INFO_MAP = new HashMap<>();
    public static final String HAT_INFO_DIRECTORY = "train_hat_info";
    public static final SynchronousResourceReloader LISTENER = TrainHatInfoReloadListener::registerOffsetOverrides;
    public static final TrainHatInfo DEFAULT = new TrainHatInfo("", 0, Vec3d.ZERO, 1.0F);

    private static void registerOffsetOverrides(ResourceManager manager) {
        ENTITY_INFO_MAP.clear();

        ResourceFinder converter = ResourceFinder.json(HAT_INFO_DIRECTORY);
        converter.findResources(manager).forEach((location, resource) -> {
            String[] splitPath = location.getPath().split("/");
            Identifier entityName = Identifier.of(location.getNamespace(), splitPath[splitPath.length - 1].replace(".json", ""));
            if (!Registries.ENTITY_TYPE.containsId(entityName)) {
                Create.LOGGER.error("Failed to load train hat info for entity {} as it does not exist.", entityName);
                return;
            }

            try (BufferedReader reader = resource.getReader()) {
                JsonObject json = JsonHelper.deserialize(reader);
                ENTITY_INFO_MAP.put(
                    Registries.ENTITY_TYPE.get(entityName),
                    TrainHatInfo.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(Create.LOGGER::error).orElseThrow()
                );
            } catch (Exception e) {
                Create.LOGGER.error("Failed to read train hat info for entity {}!", entityName, e);
            }
        });
        Create.LOGGER.info("Loaded {} train hat configurations.", ENTITY_INFO_MAP.size());
    }

    public static TrainHatInfo getHatInfoFor(Entity entity) {
        // Manual override for snow golems, they are a special case when they have a pumpkin on their head
        if (entity instanceof SnowGolemEntity snowGolem && snowGolem.hasPumpkin()) {
            return new TrainHatInfo("", 0, new Vec3d(0.0F, -3.0F, 0.0F), 1.18F);
        }

        return ENTITY_INFO_MAP.getOrDefault(entity.getType(), DEFAULT);
    }
}
