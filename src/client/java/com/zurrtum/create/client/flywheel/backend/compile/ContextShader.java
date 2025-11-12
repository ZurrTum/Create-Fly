package com.zurrtum.create.client.flywheel.backend.compile;

import com.zurrtum.create.client.flywheel.backend.Samplers;
import com.zurrtum.create.client.flywheel.backend.compile.core.Compilation;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;

public enum ContextShader {
    DEFAULT(
        null, $ -> {
    }
    ),
    CRUMBLING("_FLW_CRUMBLING", program -> program.setSamplerBinding("_flw_crumblingTex", Samplers.CRUMBLING)),
    EMBEDDED(
        "FLW_EMBEDDED", $ -> {
    }
    );

    @Nullable
    private final String define;
    private final Consumer<GlProgram> onLink;

    ContextShader(@Nullable String define, Consumer<GlProgram> onLink) {
        this.define = define;
        this.onLink = onLink;
    }

    public void onLink(GlProgram program) {
        onLink.accept(program);
    }

    public void onCompile(Compilation comp) {
        if (define != null) {
            comp.define(define);
        }
    }

    public String nameLowerCase() {
        return name().toLowerCase(Locale.ROOT);
    }
}
