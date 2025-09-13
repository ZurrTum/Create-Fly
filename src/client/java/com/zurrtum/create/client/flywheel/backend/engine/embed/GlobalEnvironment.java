package com.zurrtum.create.client.flywheel.backend.engine.embed;

import com.zurrtum.create.client.flywheel.backend.compile.ContextShader;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;

public class GlobalEnvironment implements Environment {
    public static final GlobalEnvironment INSTANCE = new GlobalEnvironment();

    private GlobalEnvironment() {
    }

    @Override
    public ContextShader contextShader() {
        return ContextShader.DEFAULT;
    }

    @Override
    public void setupDraw(GlProgram drawProgram) {
    }

    @Override
    public int matrixIndex() {
        return 0;
    }
}
