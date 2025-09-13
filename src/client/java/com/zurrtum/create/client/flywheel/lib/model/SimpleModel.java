package com.zurrtum.create.client.flywheel.lib.model;

import com.zurrtum.create.client.flywheel.api.model.Model;
import org.joml.Vector4fc;

import java.util.List;

public class SimpleModel implements Model {
    private final List<ConfiguredMesh> meshes;
    private final Vector4fc boundingSphere;

    public SimpleModel(List<ConfiguredMesh> meshes) {
        this.meshes = meshes;
        this.boundingSphere = ModelUtil.computeBoundingSphere(meshes);
    }

    @Override
    public List<ConfiguredMesh> meshes() {
        return meshes;
    }

    @Override
    public Vector4fc boundingSphere() {
        return boundingSphere;
    }
}
