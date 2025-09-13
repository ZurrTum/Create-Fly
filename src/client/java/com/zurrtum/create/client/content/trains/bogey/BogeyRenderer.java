package com.zurrtum.create.client.content.trains.bogey;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;

public interface BogeyRenderer {
    void render(
        NbtCompound bogeyData,
        float wheelAngle,
        float partialTick,
        MatrixStack poseStack,
        VertexConsumerProvider bufferSource,
        int packedLight,
        int packedOverlay,
        boolean inContraption
    );
}
