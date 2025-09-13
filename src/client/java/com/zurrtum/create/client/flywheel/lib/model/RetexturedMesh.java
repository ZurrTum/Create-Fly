package com.zurrtum.create.client.flywheel.lib.model;

import com.zurrtum.create.client.flywheel.api.model.IndexSequence;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.api.vertex.MutableVertexList;
import com.zurrtum.create.client.flywheel.lib.vertex.VertexTransformations;
import net.minecraft.client.texture.Sprite;
import org.joml.Vector4fc;

public record RetexturedMesh(Mesh mesh, Sprite sprite) implements Mesh {
    @Override
    public int vertexCount() {
        return mesh.vertexCount();
    }

    @Override
    public void write(MutableVertexList vertexList) {
        mesh.write(vertexList);
        VertexTransformations.retexture(vertexList, sprite);
    }

    @Override
    public IndexSequence indexSequence() {
        return mesh.indexSequence();
    }

    @Override
    public int indexCount() {
        return mesh.indexCount();
    }

    @Override
    public Vector4fc boundingSphere() {
        return mesh.boundingSphere();
    }
}

