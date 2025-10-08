package com.zurrtum.create.client.flywheel.lib.model.part;

import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.model.RetexturedMesh;
import com.zurrtum.create.client.flywheel.lib.model.SingleMeshModel;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ModelTrees {
    private static final RendererReloadCache<ModelTreeKey, ModelTree> CACHE = new RendererReloadCache<>(k -> {
        ModelTree tree = convert(
            "",
            MeshTree.of(k.layer),
            k.pathsToPrune,
            k.texture != null ? MinecraftClient.getInstance().getAtlasManager().getSprite(k.texture) : null,
            k.material
        );

        if (tree == null) {
            throw new IllegalArgumentException("Cannot prune root node!");
        }

        return tree;
    });

    private ModelTrees() {
    }

    public static ModelTree of(EntityModelLayer layer, Material material) {
        return CACHE.get(new ModelTreeKey(layer, Collections.emptySet(), null, material));
    }

    public static ModelTree of(EntityModelLayer layer, SpriteIdentifier texture, Material material) {
        return CACHE.get(new ModelTreeKey(layer, Collections.emptySet(), texture, material));
    }

    public static ModelTree of(EntityModelLayer layer, Set<String> pathsToPrune, Material material) {
        return CACHE.get(new ModelTreeKey(layer, Set.copyOf(pathsToPrune), null, material));
    }

    public static ModelTree of(EntityModelLayer layer, Set<String> pathsToPrune, SpriteIdentifier texture, Material material) {
        return CACHE.get(new ModelTreeKey(layer, Set.copyOf(pathsToPrune), texture, material));
    }

    @Nullable
    private static ModelTree convert(String path, MeshTree meshTree, Set<String> pathsToPrune, @Nullable Sprite sprite, Material material) {
        if (pathsToPrune.contains(path)) {
            return null;
        }

        Model model = null;
        Mesh mesh = meshTree.mesh();

        if (mesh != null) {
            if (sprite != null) {
                mesh = new RetexturedMesh(mesh, sprite);
            }

            model = new SingleMeshModel(mesh, material);
        }

        Map<String, ModelTree> children = new HashMap<>();
        String pathSlash = path + "/";

        for (int i = 0; i < meshTree.childCount(); i++) {
            String childName = meshTree.childName(i);
            var child = convert(pathSlash + childName, meshTree.child(i), pathsToPrune, sprite, material);

            if (child != null) {
                children.put(childName, child);
            }
        }

        return new ModelTree(model, meshTree.initialPose(), children);
    }

    private record ModelTreeKey(
        EntityModelLayer layer, Set<String> pathsToPrune, @Nullable SpriteIdentifier texture, Material material
    ) {
    }
}