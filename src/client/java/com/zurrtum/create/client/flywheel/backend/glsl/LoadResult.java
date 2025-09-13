package com.zurrtum.create.client.flywheel.backend.glsl;

import com.zurrtum.create.client.flywheel.backend.compile.core.ShaderException;

public sealed interface LoadResult {
    SourceFile unwrap();

    public static record Success(SourceFile source) implements LoadResult {
        public Success(SourceFile source) {
            this.source = source;
        }

        public SourceFile unwrap() {
            return this.source;
        }

        public SourceFile source() {
            return this.source;
        }
    }

    public static record Failure(LoadError error) implements LoadResult {
        public Failure(LoadError error) {
            this.error = error;
        }

        public SourceFile unwrap() {
            throw new ShaderException.Load(this.error.generateMessage().build());
        }

        public LoadError error() {
            return this.error;
        }
    }
}
