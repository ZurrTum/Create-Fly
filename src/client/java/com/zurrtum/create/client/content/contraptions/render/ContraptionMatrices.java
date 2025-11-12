package com.zurrtum.create.client.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.content.contraptions.render.ContraptionEntityRenderer.AbstractContraptionState;
import org.joml.Matrix4f;

/**
 * <p>
 * ContraptionMatrices must be cleared and setup per-contraption per-frame
 * </p>
 */
public class ContraptionMatrices {

    private final PoseStack modelViewProjection = new PoseStack();
    private final PoseStack viewProjection = new PoseStack();
    private final PoseStack model = new PoseStack();
    private final Matrix4f world = new Matrix4f();
    private final Matrix4f light = new Matrix4f();

    <S extends AbstractContraptionState> void setup(ContraptionEntityRenderer<?, S> renderer, PoseStack viewProjection, S state) {
        this.viewProjection.pushPose();
        transform(this.viewProjection, viewProjection);
        model.pushPose();
        renderer.transform(state, model);

        modelViewProjection.pushPose();
        transform(modelViewProjection, viewProjection);
        transform(modelViewProjection, model);

        translateToEntity(world, state);

        light.set(world);
        light.mul(model.last().pose());
    }

    void clear() {
        clearStack(modelViewProjection);
        clearStack(viewProjection);
        clearStack(model);
        world.identity();
        light.identity();
    }

    public PoseStack getModelViewProjection() {
        return modelViewProjection;
    }

    public PoseStack getViewProjection() {
        return viewProjection;
    }

    public PoseStack getModel() {
        return model;
    }

    public Matrix4f getWorld() {
        return world;
    }

    public Matrix4f getLight() {
        return light;
    }

    public static void transform(PoseStack ms, PoseStack transform) {
        ms.last().pose().mul(transform.last().pose());
        ms.last().normal().mul(transform.last().normal());
    }

    public static void translateToEntity(Matrix4f matrix, AbstractContraptionState state) {
        matrix.setTranslation((float) state.x, (float) state.y, (float) state.z);
    }

    public static void clearStack(PoseStack ms) {
        while (!ms.isEmpty()) {
            ms.popPose();
        }
    }

}
