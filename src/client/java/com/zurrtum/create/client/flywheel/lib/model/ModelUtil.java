package com.zurrtum.create.client.flywheel.lib.model;

import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.vertex.VertexList;
import com.zurrtum.create.client.flywheel.lib.material.Materials;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import com.zurrtum.create.client.flywheel.lib.vertex.PosVertexView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Collection;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public final class ModelUtil {
    private static final float BOUNDING_SPHERE_EPSILON = 1e-4f;

    private ModelUtil() {
    }

    @Nullable
    public static Material getMaterial(ChunkSectionLayer chunkRenderType, boolean shaded) {
        if (chunkRenderType == ChunkSectionLayer.SOLID) {
            return shaded ? Materials.SOLID_BLOCK : Materials.SOLID_UNSHADED_BLOCK;
        }
        if (chunkRenderType == ChunkSectionLayer.CUTOUT_MIPPED) {
            return shaded ? Materials.CUTOUT_MIPPED_BLOCK : Materials.CUTOUT_MIPPED_UNSHADED_BLOCK;
        }
        if (chunkRenderType == ChunkSectionLayer.CUTOUT) {
            return shaded ? Materials.CUTOUT_BLOCK : Materials.CUTOUT_UNSHADED_BLOCK;
        }
        if (chunkRenderType == ChunkSectionLayer.TRANSLUCENT) {
            return shaded ? Materials.TRANSLUCENT_BLOCK : Materials.TRANSLUCENT_UNSHADED_BLOCK;
        }
        if (chunkRenderType == ChunkSectionLayer.TRIPWIRE) {
            return shaded ? Materials.TRIPWIRE_BLOCK : Materials.TRIPWIRE_UNSHADED_BLOCK;
        }
        return null;
    }

    @Nullable
    public static Material getItemMaterial(RenderType renderType) {
        if (renderType == RenderType.solid()) {
            return Materials.SOLID_BLOCK;
        }
        if (renderType == RenderType.cutoutMipped()) {
            return Materials.CUTOUT_MIPPED_BLOCK;
        }
        if (renderType == RenderType.cutout()) {
            return Materials.CUTOUT_BLOCK;
        }
        if (renderType == RenderType.tripwire()) {
            return Materials.TRIPWIRE_BLOCK;
        }

        if (renderType == Sheets.cutoutBlockSheet()) {
            return Materials.CUTOUT_BLOCK;
        }

        if (renderType == Sheets.solidBlockSheet()) {
            return Materials.SOLID_BLOCK;
        }

        if (renderType == Sheets.translucentItemSheet()) {
            return Materials.TRANSLUCENT_ENTITY;
        }

        if (renderType == RenderType.glint()) {
            return Materials.GLINT;
        }
        if (renderType == RenderType.glintTranslucent()) {
            return Materials.TRANSLUCENT_GLINT;
        }
        if (renderType == RenderType.entityGlint()) {
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
