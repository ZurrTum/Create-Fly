package com.zurrtum.create.client.flywheel.lib.model;

import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.vertex.VertexList;
import com.zurrtum.create.client.flywheel.lib.material.Materials;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import com.zurrtum.create.client.flywheel.lib.vertex.PosVertexView;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Collection;

public final class ModelUtil {
    private static final float BOUNDING_SPHERE_EPSILON = 1e-4f;

    private ModelUtil() {
    }

    @Nullable
    public static Material getMaterial(BlockRenderLayer chunkRenderType, boolean shaded) {
        if (chunkRenderType == BlockRenderLayer.SOLID) {
            return shaded ? Materials.SOLID_BLOCK : Materials.SOLID_UNSHADED_BLOCK;
        }
        if (chunkRenderType == BlockRenderLayer.CUTOUT_MIPPED) {
            return shaded ? Materials.CUTOUT_MIPPED_BLOCK : Materials.CUTOUT_MIPPED_UNSHADED_BLOCK;
        }
        if (chunkRenderType == BlockRenderLayer.CUTOUT) {
            return shaded ? Materials.CUTOUT_BLOCK : Materials.CUTOUT_UNSHADED_BLOCK;
        }
        if (chunkRenderType == BlockRenderLayer.TRANSLUCENT) {
            return shaded ? Materials.TRANSLUCENT_BLOCK : Materials.TRANSLUCENT_UNSHADED_BLOCK;
        }
        if (chunkRenderType == BlockRenderLayer.TRIPWIRE) {
            return shaded ? Materials.TRIPWIRE_BLOCK : Materials.TRIPWIRE_UNSHADED_BLOCK;
        }
        return null;
    }

    @Nullable
    public static Material getItemMaterial(RenderLayer renderType) {
        if (renderType == RenderLayer.getSolid()) {
            return Materials.SOLID_BLOCK;
        }
        if (renderType == RenderLayer.getCutoutMipped()) {
            return Materials.CUTOUT_MIPPED_BLOCK;
        }
        if (renderType == RenderLayer.getCutout()) {
            return Materials.CUTOUT_BLOCK;
        }
        if (renderType == RenderLayer.getTripwire()) {
            return Materials.TRIPWIRE_BLOCK;
        }

        if (renderType == TexturedRenderLayers.getEntityCutout()) {
            return Materials.CUTOUT_BLOCK;
        }

        if (renderType == TexturedRenderLayers.getEntitySolid()) {
            return Materials.SOLID_BLOCK;
        }

        if (renderType == TexturedRenderLayers.getItemEntityTranslucentCull()) {
            return Materials.TRANSLUCENT_ENTITY;
        }

        if (renderType == RenderLayer.getGlint()) {
            return Materials.GLINT;
        }
        if (renderType == RenderLayer.getGlintTranslucent()) {
            return Materials.TRANSLUCENT_GLINT;
        }
        if (renderType == RenderLayer.getEntityGlint()) {
            return Materials.GLINT_ENTITY;
        }
        return null;
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
