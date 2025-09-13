package com.zurrtum.create.client.flywheel.impl.event;

import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public record RenderContextImpl(
    WorldRenderer renderer, ClientWorld level, BufferBuilderStorage buffers, Matrix4fc modelView, Matrix4fc projection, Matrix4fc viewProjection,
    Camera camera, float partialTick
) implements RenderContext {
    public static RenderContextImpl create(
        WorldRenderer renderer,
        ClientWorld level,
        BufferBuilderStorage buffers,
        Matrix4fc modelView,
        Matrix4f projection,
        Camera camera,
        float partialTick
    ) {
        Matrix4f viewProjection = new Matrix4f(projection);
        viewProjection.mul(modelView);

        return new RenderContextImpl(renderer, level, buffers, modelView, projection, viewProjection, camera, partialTick);
    }
}
