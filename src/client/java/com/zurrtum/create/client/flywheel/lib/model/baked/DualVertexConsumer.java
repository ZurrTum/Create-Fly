package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;

public class DualVertexConsumer extends VertexConsumers.Dual {
    public DualVertexConsumer(VertexConsumer first, VertexConsumer second) {
        super(first, second);
    }

    @Override
    public void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, float f, int i, int j) {
        first.quad(matrixEntry, quad, red, green, blue, f, i, j);
        second.quad(matrixEntry, quad, red, green, blue, f, i, j);
    }

    @Override
    public void quad(
        MatrixStack.Entry matrixEntry,
        BakedQuad quad,
        float[] brightnesses,
        float red,
        float green,
        float blue,
        float f,
        int[] is,
        int i,
        boolean bl
    ) {
        first.quad(matrixEntry, quad, brightnesses, red, green, blue, f, is, i, bl);
        second.quad(matrixEntry, quad, brightnesses, red, green, blue, f, is, i, bl);
    }

    public void emit(ModelPart part, MatrixStack matrices, Sprite sprite, int light, int overlay, int color) {
        ((ItemMeshEmitter) second).emit(part, matrices, sprite, (ItemMeshEmitter) first, light, overlay, color);
    }
}
