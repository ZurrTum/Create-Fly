package com.zurrtum.create.client.flywheel.api.visualization;

import com.zurrtum.create.client.flywheel.api.backend.BackendImplemented;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

@BackendImplemented
public interface VisualEmbedding extends VisualizationContext {
    void transforms(Matrix4fc var1, Matrix3fc var2);

    void delete();
}
