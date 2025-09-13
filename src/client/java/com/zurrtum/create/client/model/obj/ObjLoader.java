/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model.obj;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.zurrtum.create.client.model.StandardModelParameters;
import com.zurrtum.create.client.model.UnbakedModelLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * A loader for {@link ObjModel OBJ models}.
 * <p>
 * Allows the user to enable automatic face culling, toggle quad shading, flip UVs, render emissively and specify a
 * {@link ObjMaterialLibrary material library} override.
 */
public class ObjLoader implements UnbakedModelLoader<ObjModel>, SynchronousResourceReloader {
    public static ObjLoader INSTANCE = new ObjLoader();

    private final Map<ObjGeometry.Settings, ObjGeometry> geometryCache = Maps.newConcurrentMap();
    private final Map<Identifier, ObjMaterialLibrary> materialCache = Maps.newConcurrentMap();

    private final ResourceManager manager = MinecraftClient.getInstance().getResourceManager();

    @Override
    public void reload(ResourceManager resourceManager) {
        geometryCache.clear();
        materialCache.clear();
    }

    @Override
    public ObjModel read(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (!jsonObject.has("model"))
            throw new JsonParseException("OBJ Loader requires a 'model' key that points to a valid .OBJ model.");

        String modelLocation = jsonObject.get("model").getAsString();

        boolean automaticCulling = JsonHelper.getBoolean(jsonObject, "automatic_culling", true);
        boolean shadeQuads = JsonHelper.getBoolean(jsonObject, "shade_quads", true);
        boolean flipV = JsonHelper.getBoolean(jsonObject, "flip_v", false);
        boolean emissiveAmbient = JsonHelper.getBoolean(jsonObject, "emissive_ambient", true);
        String mtlOverride = JsonHelper.getString(jsonObject, "mtl_override", null);
        StandardModelParameters parameters = StandardModelParameters.parse(jsonObject, jsonDeserializationContext);

        var geometry = loadGeometry(new ObjGeometry.Settings(
            Identifier.of(modelLocation),
            automaticCulling,
            shadeQuads,
            flipV,
            emissiveAmbient,
            mtlOverride,
            parameters
        ));
        return new ObjModel(parameters, geometry);
    }

    public ObjGeometry loadGeometry(ObjGeometry.Settings settings) {
        return geometryCache.computeIfAbsent(
            settings, (data) -> {
                Resource resource = manager.getResource(settings.modelLocation()).orElseThrow();
                try (ObjTokenizer tokenizer = new ObjTokenizer(resource.getInputStream())) {
                    return ObjGeometry.parse(tokenizer, data);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Could not find OBJ model", e);
                } catch (Exception e) {
                    throw new RuntimeException("Could not read OBJ model", e);
                }
            }
        );
    }

    public ObjMaterialLibrary loadMaterialLibrary(Identifier materialLocation) {
        return materialCache.computeIfAbsent(
            materialLocation, (location) -> {
                Resource resource = manager.getResource(location).orElseThrow();
                try (ObjTokenizer rdr = new ObjTokenizer(resource.getInputStream())) {
                    return new ObjMaterialLibrary(rdr);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Could not find OBJ material library", e);
                } catch (Exception e) {
                    throw new RuntimeException("Could not read OBJ material library", e);
                }
            }
        );
    }
}
