package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.contraptions.render.ContraptionEntityRenderer.AbstractContraptionState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.util.function.BiConsumer;

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

    void setup(BiConsumer<MatrixStack, Float> transform, MatrixStack viewProjection, AbstractContraptionState state) {
        float partialTicks = AnimationTickHolder.getPartialTicks();

        this.viewProjection.push();
        transform(this.viewProjection, viewProjection);
        model.push();
        transform.accept(model, partialTicks);

        modelViewProjection.push();
        transform(modelViewProjection, viewProjection);
        transform(modelViewProjection, model);

        translateToEntity(world, state, partialTicks);

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

    public static void translateToEntity(Matrix4f matrix, AbstractContraptionState state, float partialTicks) {
        double x = MathHelper.lerp(partialTicks, state.lastRenderX, state.entityX);
        double y = MathHelper.lerp(partialTicks, state.lastRenderY, state.entityY);
        double z = MathHelper.lerp(partialTicks, state.lastRenderZ, state.entityZ);
        matrix.setTranslation((float) x, (float) y, (float) z);
    }

    public static void clearStack(MatrixStack ms) {
        while (!ms.isEmpty()) {
            ms.pop();
        }
    }

}
