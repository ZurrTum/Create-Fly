package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.client.content.contraptions.render.ContraptionEntityRenderer.AbstractContraptionState;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

/**
 * <p>
 * ContraptionMatrices must be cleared and setup per-contraption per-frame
 * </p>
 */
public class ContraptionMatrices {

    private final MatrixStack modelViewProjection = new MatrixStack();
    private final MatrixStack viewProjection = new MatrixStack();
    private final MatrixStack model = new MatrixStack();
    private final Matrix4f world = new Matrix4f();
    private final Matrix4f light = new Matrix4f();

    <S extends AbstractContraptionState> void setup(ContraptionEntityRenderer<?, S> renderer, MatrixStack viewProjection, S state) {
        this.viewProjection.push();
        transform(this.viewProjection, viewProjection);
        model.push();
        renderer.transform(state, model);

        modelViewProjection.push();
        transform(modelViewProjection, viewProjection);
        transform(modelViewProjection, model);

        translateToEntity(world, state);

        light.set(world);
        light.mul(model.peek().getPositionMatrix());
    }

    void clear() {
        clearStack(modelViewProjection);
        clearStack(viewProjection);
        clearStack(model);
        world.identity();
        light.identity();
    }

    public MatrixStack getModelViewProjection() {
        return modelViewProjection;
    }

    public MatrixStack getViewProjection() {
        return viewProjection;
    }

    public MatrixStack getModel() {
        return model;
    }

    public Matrix4f getWorld() {
        return world;
    }

    public Matrix4f getLight() {
        return light;
    }

    public static void transform(MatrixStack ms, MatrixStack transform) {
        ms.peek().getPositionMatrix().mul(transform.peek().getPositionMatrix());
        ms.peek().getNormalMatrix().mul(transform.peek().getNormalMatrix());
    }

    public static void translateToEntity(Matrix4f matrix, AbstractContraptionState state) {
        matrix.setTranslation((float) state.x, (float) state.y, (float) state.z);
    }

    public static void clearStack(MatrixStack ms) {
        while (!ms.isEmpty()) {
            ms.pop();
        }
    }

}
