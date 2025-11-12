package com.zurrtum.create.client.flywheel.api.backend;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4fc;

public interface RenderContext {
    LevelRenderer renderer();

    ClientLevel level();

    RenderBuffers buffers();

    Matrix4fc modelView();

    Matrix4fc projection();

    Matrix4fc viewProjection();

    Camera camera();

    float partialTick();
}
