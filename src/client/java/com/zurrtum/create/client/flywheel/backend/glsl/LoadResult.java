package com.zurrtum.create.client.flywheel.backend.glsl;

import com.zurrtum.create.client.flywheel.backend.compile.core.ShaderException;

public sealed interface LoadResult {
    SourceFile unwrap();

    record Success(SourceFile source) implements LoadResult {
        public SourceFile unwrap() {
            return this.source;
        }
    }

    record Failure(LoadError error) implements LoadResult {
        public SourceFile unwrap() {
            throw new ShaderException.Load(this.error.generateMessage().build());
        }
    }
}
