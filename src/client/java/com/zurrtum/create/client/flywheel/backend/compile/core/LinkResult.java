package com.zurrtum.create.client.flywheel.backend.compile.core;

import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;

public sealed interface LinkResult {
    GlProgram unwrap();

    record Success(GlProgram program, String log) implements LinkResult {
        @Override
        public GlProgram unwrap() {
            return program;
        }
    }

    record Failure(String failure) implements LinkResult {
        @Override
        public GlProgram unwrap() {
            throw new ShaderException.Link(failure);
        }
    }

    static LinkResult success(GlProgram program, String log) {
        return new Success(program, log);
    }

    static LinkResult failure(String failure) {
        return new Failure(failure);
    }
}
