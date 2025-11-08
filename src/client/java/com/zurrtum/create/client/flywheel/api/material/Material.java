package com.zurrtum.create.client.flywheel.api.material;

import net.minecraft.util.Identifier;

public interface Material {
    MaterialShaders shaders();

    FogShader fog();

    CutoutShader cutout();

    LightShader light();

    Identifier texture();

    boolean blur();

    boolean mipmap();

    boolean backfaceCulling();

    boolean polygonOffset();

    DepthTest depthTest();

    Transparency transparency();

    WriteMask writeMask();

    boolean useOverlay();

    boolean useLight();

    CardinalLightingMode cardinalLightingMode();
}
