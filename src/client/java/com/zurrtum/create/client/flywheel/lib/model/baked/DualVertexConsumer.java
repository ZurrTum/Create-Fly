package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class DualVertexConsumer extends VertexMultiConsumer.Double {
    public DualVertexConsumer(VertexConsumer first, VertexConsumer second) {
        super(first, second);
    }

    @Override
    public void putBulkData(PoseStack.Pose matrixEntry, BakedQuad quad, float red, float green, float blue, float f, int i, int j) {
        first.putBulkData(matrixEntry, quad, red, green, blue, f, i, j);
        second.putBulkData(matrixEntry, quad, red, green, blue, f, i, j);
    }

    @Override
    public void putBulkData(
        PoseStack.Pose matrixEntry,
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
        first.putBulkData(matrixEntry, quad, brightnesses, red, green, blue, f, is, i, bl);
        second.putBulkData(matrixEntry, quad, brightnesses, red, green, blue, f, is, i, bl);
    }

    public void emit(ModelPart part, PoseStack matrices, TextureAtlasSprite sprite, int light, int overlay, int color) {
        ((ItemMeshEmitter) second).emit(part, matrices, sprite, (ItemMeshEmitter) first, light, overlay, color);
    }
}
