package com.zurrtum.create.client.flywheel.backend.compile.core;

import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlShader;

import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;

public class ProgramLinker {

    public ProgramLinker() {
    }

    public GlProgram link(List<GlShader> shaders, Consumer<GlProgram> preLink) {
        // this probably doesn't need caching
        return linkInternal(shaders, preLink).unwrap();
    }

    private LinkResult linkInternal(List<GlShader> shaders, Consumer<GlProgram> preLink) {
        int handle = glCreateProgram();
        var out = new GlProgram(handle);

        for (GlShader shader : shaders) {
            glAttachShader(handle, shader.handle());
        }

        preLink.accept(out);

        glLinkProgram(handle);
        String log = glGetProgramInfoLog(handle);

        if (linkSuccessful(handle)) {
            return LinkResult.success(out, log);
        } else {
            out.delete();
            return LinkResult.failure(log);
        }
    }

    private static boolean linkSuccessful(int handle) {
        return glGetProgrami(handle, GL_LINK_STATUS) == GL_TRUE;
    }

}
