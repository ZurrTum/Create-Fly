package com.zurrtum.create.client.flywheel.api.backend;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Matrix4fc;

public interface RenderContext {
    WorldRenderer renderer();

    ClientWorld level();

    BufferBuilderStorage buffers();

    Matrix4fc modelView();

    Matrix4fc projection();

    Matrix4fc viewProjection();

    Camera camera();

    float partialTick();
}
