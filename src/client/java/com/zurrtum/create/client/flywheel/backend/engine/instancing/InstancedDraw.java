package com.zurrtum.create.client.flywheel.backend.engine.instancing;

import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.backend.engine.AbstractDraw;
import com.zurrtum.create.client.flywheel.backend.engine.GpuTextureManager;
import com.zurrtum.create.client.flywheel.backend.engine.GroupKey;
import com.zurrtum.create.client.flywheel.backend.engine.MeshPool;
import com.zurrtum.create.client.flywheel.backend.gl.TextureBuffer;

public class InstancedDraw implements AbstractDraw {
    public final GroupKey groupKey;
    private final InstancedInstancer<?> instancer;
    private final MeshPool.PooledMesh mesh;
    private final GpuTextureManager textureManager;
    private final Material material;
    private final int bias;
    private final int indexOfMeshInModel;

    private boolean deleted;

    public InstancedDraw(
        InstancedInstancer<?> instancer,
        MeshPool.PooledMesh mesh,
        GroupKey groupKey,
        GpuTextureManager textureManager,
        Material material,
        int bias,
        int indexOfMeshInModel
    ) {
        this.instancer = instancer;
        this.mesh = mesh;
        this.groupKey = groupKey;
        this.textureManager = textureManager;
        this.material = material;
        this.bias = bias;
        this.indexOfMeshInModel = indexOfMeshInModel;

        mesh.acquire();
        textureManager.add(material);
    }

    public int bias() {
        return bias;
    }

    public int indexOfMeshInModel() {
        return indexOfMeshInModel;
    }

    @Override
    public Material material() {
        return material;
    }

    public boolean deleted() {
        return deleted;
    }

    public MeshPool.PooledMesh mesh() {
        return mesh;
    }

    public void render(TextureBuffer buffer) {
        if (mesh.isInvalid()) {
            return;
        }

        instancer.bind(buffer);

        mesh.draw(instancer.instanceCount());
    }

    public void renderOne(TextureBuffer buffer) {
        if (mesh.isInvalid()) {
            return;
        }

        instancer.bind(buffer);

        mesh.draw(1);
    }

    public void delete() {
        if (deleted) {
            return;
        }

        mesh.release();
        textureManager.remove(material);

        deleted = true;
    }
}
