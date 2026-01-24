package com.zurrtum.create.client.flywheel.lib.model;

import com.zurrtum.create.client.flywheel.api.material.CardinalLightingMode;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.vertex.VertexList;
import com.zurrtum.create.client.flywheel.lib.material.Materials;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import com.zurrtum.create.client.flywheel.lib.vertex.PosVertexView;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ModelUtil {
    private static final float BOUNDING_SPHERE_EPSILON = 1e-4f;

    // Array of chunk materials to make lookups easier.
    // Index by (renderTypeIdx * 4 + shaded * 2 + ambientOcclusion).
    private static final Material[] CHUNK_MATERIALS = new Material[12];
    private static final Map<RenderType, Material> ITEM_CHUNK_MATERIALS = new IdentityHashMap<>();

    static {
        Material[] baseChunkMaterials = new Material[]{Materials.SOLID_BLOCK, Materials.CUTOUT_BLOCK, Materials.TRANSLUCENT_BLOCK};
        for (int chunkLayerIdx = 0, size = baseChunkMaterials.length; chunkLayerIdx < size; chunkLayerIdx++) {
            int baseMaterialIdx = chunkLayerIdx * 4;
            Material baseChunkMaterial = baseChunkMaterials[chunkLayerIdx];

            // shaded: false, ambientOcclusion: false
            CHUNK_MATERIALS[baseMaterialIdx] = SimpleMaterial.builderOf(baseChunkMaterial).cardinalLightingMode(CardinalLightingMode.OFF)
                .ambientOcclusion(false).build();
            // shaded: false, ambientOcclusion: true
            CHUNK_MATERIALS[baseMaterialIdx + 1] = SimpleMaterial.builderOf(baseChunkMaterial).cardinalLightingMode(CardinalLightingMode.OFF).build();
            // shaded: true, ambientOcclusion: false
            CHUNK_MATERIALS[baseMaterialIdx + 2] = SimpleMaterial.builderOf(baseChunkMaterial).ambientOcclusion(false).build();
            // shaded: true, ambientOcclusion: true
            CHUNK_MATERIALS[baseMaterialIdx + 3] = baseChunkMaterial;
        }
        ITEM_CHUNK_MATERIALS.put(RenderTypes.solidMovingBlock(), CHUNK_MATERIALS[2]);
        ITEM_CHUNK_MATERIALS.put(RenderTypes.cutoutMovingBlock(), CHUNK_MATERIALS[6]);
        ITEM_CHUNK_MATERIALS.put(RenderTypes.translucentMovingBlock(), CHUNK_MATERIALS[10]);
        ITEM_CHUNK_MATERIALS.put(Sheets.cutoutBlockSheet(), CHUNK_MATERIALS[7]);
        ITEM_CHUNK_MATERIALS.put(Sheets.solidBlockSheet(), CHUNK_MATERIALS[3]);
        ITEM_CHUNK_MATERIALS.put(Sheets.translucentBlockItemSheet(), Materials.TRANSLUCENT_ITEM_ENTITY_BLOCK);
        ITEM_CHUNK_MATERIALS.put(Sheets.translucentItemSheet(), Materials.TRANSLUCENT_ITEM_ENTITY_ITEM);
        ITEM_CHUNK_MATERIALS.put(RenderTypes.glint(), Materials.GLINT);
        ITEM_CHUNK_MATERIALS.put(RenderTypes.glintTranslucent(), Materials.GLINT);
        ITEM_CHUNK_MATERIALS.put(RenderTypes.entityGlint(), Materials.GLINT_ENTITY);
    }

    private ModelUtil() {
    }

    public static Material getMaterial(ChunkSectionLayer chunkRenderType, boolean shaded, boolean ambientOcclusion) {
        int materialIdx = chunkRenderType.ordinal() * 4;
        if (ambientOcclusion) {
            materialIdx++;
        }
        if (shaded) {
            materialIdx += 2;
        }
        return CHUNK_MATERIALS[materialIdx];
    }

    @Nullable
    public static Material getItemMaterial(RenderType renderType) {
        return ITEM_CHUNK_MATERIALS.get(renderType);
    }

    public static int computeTotalVertexCount(Iterable<Mesh> meshes) {
        int vertexCount = 0;
        for (Mesh mesh : meshes) {
            vertexCount += mesh.vertexCount();
        }
        return vertexCount;
    }

    public static Vector4f computeBoundingSphere(Collection<Model.ConfiguredMesh> meshes) {
        return computeBoundingSphere(meshes.stream().map(Model.ConfiguredMesh::mesh).toList());
    }

    public static Vector4f computeBoundingSphere(Iterable<Mesh> meshes) {
        int vertexCount = computeTotalVertexCount(meshes);
        var block = MemoryBlock.malloc((long) vertexCount * PosVertexView.STRIDE);
        var vertexList = new PosVertexView();

        int baseVertex = 0;
        for (Mesh mesh : meshes) {
            vertexList.ptr(block.ptr() + (long) baseVertex * PosVertexView.STRIDE);
            vertexList.vertexCount(mesh.vertexCount());
            mesh.write(vertexList);
            baseVertex += mesh.vertexCount();
        }

        vertexList.ptr(block.ptr());
        vertexList.vertexCount(vertexCount);
        var sphere = computeBoundingSphere(vertexList);

        block.free();

        return sphere;
    }

    public static Vector4f computeBoundingSphere(VertexList vertexList) {
        var center = computeCenterOfAABBContaining(vertexList);

        var radius = computeMaxDistanceTo(vertexList, center) + BOUNDING_SPHERE_EPSILON;

        return new Vector4f(center, radius);
    }

    private static float computeMaxDistanceTo(VertexList vertexList, Vector3f pos) {
        float farthestDistanceSquared = -1;

        for (int i = 0; i < vertexList.vertexCount(); i++) {
            var distanceSquared = pos.distanceSquared(vertexList.x(i), vertexList.y(i), vertexList.z(i));

            if (distanceSquared > farthestDistanceSquared) {
                farthestDistanceSquared = distanceSquared;
            }
        }

        return (float) Math.sqrt(farthestDistanceSquared);
    }

    private static Vector3f computeCenterOfAABBContaining(VertexList vertexList) {
        var min = new Vector3f(Float.MAX_VALUE);
        var max = new Vector3f(Float.MIN_VALUE);

        for (int i = 0; i < vertexList.vertexCount(); i++) {
            float x = vertexList.x(i);
            float y = vertexList.y(i);
            float z = vertexList.z(i);

            // JOML's min/max methods don't accept floats :whywheel:
            min.x = Math.min(min.x, x);
            min.y = Math.min(min.y, y);
            min.z = Math.min(min.z, z);

            max.x = Math.max(max.x, x);
            max.y = Math.max(max.y, y);
            max.z = Math.max(max.z, z);
        }

        return min.add(max).mul(0.5f);
    }
}
