package com.zurrtum.create.client.flywheel.backend.gl.shader;

import com.zurrtum.create.client.flywheel.backend.gl.GlObject;
import org.lwjgl.opengl.GL20;

public class GlShader extends GlObject {

    public final ShaderType type;
    private final String name;

    public GlShader(int handle, ShaderType type, String name) {
        this.type = type;
        this.name = name;

        handle(handle);
    }

    @Override
    protected void deleteInternal(int handle) {
        GL20.glDeleteShader(handle);
    }

    @Override
    public String toString() {
        return "GlShader{" + type.name + handle() + " " + name + "}";
    }

}