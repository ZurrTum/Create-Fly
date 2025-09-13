package com.zurrtum.create.client.flywheel.backend;

import com.zurrtum.create.client.flywheel.backend.gl.GlTextureUnit;

public class Samplers {
    public static final GlTextureUnit DIFFUSE = GlTextureUnit.T0;
    public static final GlTextureUnit OVERLAY = GlTextureUnit.T1;
    public static final GlTextureUnit LIGHT = GlTextureUnit.T2;
    public static final GlTextureUnit CRUMBLING = GlTextureUnit.T3;
    public static final GlTextureUnit INSTANCE_BUFFER = GlTextureUnit.T4;
    public static final GlTextureUnit LIGHT_LUT = GlTextureUnit.T5;
    public static final GlTextureUnit LIGHT_SECTIONS = GlTextureUnit.T6;

    public static final GlTextureUnit DEPTH_RANGE = GlTextureUnit.T7;
    public static final GlTextureUnit COEFFICIENTS = GlTextureUnit.T8;
    public static final GlTextureUnit NOISE = GlTextureUnit.T9;
}
