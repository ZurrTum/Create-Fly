package com.zurrtum.create.client.flywheel.backend.compile;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public record Pipeline(
    Identifier vertexMain, Identifier fragmentMain, InstanceAssembler assembler, String compilerMarker, Consumer<GlProgram> onLink
) {

    @FunctionalInterface
    public interface InstanceAssembler {
        /**
         * Generate the source component necessary to convert a packed {@link Instance} into its shader representation.
         *
         * @return A source component defining functions that unpack a representation of the given instance type.
         */
        SourceComponent assemble(InstanceType<?> instanceType);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private Identifier vertexMain;
        @Nullable
        private Identifier fragmentMain;
        @Nullable
        private InstanceAssembler assembler;
        @Nullable
        private String compilerMarker;
        @Nullable
        private Consumer<GlProgram> onLink;

        public Builder vertexMain(Identifier shader) {
            this.vertexMain = shader;
            return this;
        }

        public Builder fragmentMain(Identifier shader) {
            this.fragmentMain = shader;
            return this;
        }

        public Builder assembler(InstanceAssembler assembler) {
            this.assembler = assembler;
            return this;
        }

        public Builder compilerMarker(String compilerMarker) {
            this.compilerMarker = compilerMarker;
            return this;
        }

        public Builder onLink(Consumer<GlProgram> onLink) {
            this.onLink = onLink;
            return this;
        }

        public Pipeline build() {
            Objects.requireNonNull(vertexMain);
            Objects.requireNonNull(fragmentMain);
            Objects.requireNonNull(assembler);
            Objects.requireNonNull(compilerMarker);
            Objects.requireNonNull(onLink);
            return new Pipeline(vertexMain, fragmentMain, assembler, compilerMarker, onLink);
        }
    }
}
