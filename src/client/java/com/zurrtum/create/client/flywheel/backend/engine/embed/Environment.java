package com.zurrtum.create.client.flywheel.backend.engine.embed;

import com.zurrtum.create.client.flywheel.backend.compile.ContextShader;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;

public interface Environment {
    ContextShader contextShader();

    void setupDraw(GlProgram drawProgram);

    int matrixIndex();
}
