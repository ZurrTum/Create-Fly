package com.zurrtum.create.client.foundation.blockEntity.renderer;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class SafeBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    @Override
    public final void render(T be, float partialTicks, MatrixStack ms, VertexConsumerProvider bufferSource, int light, int overlay, Vec3d cameraPos) {
        if (isInvalid(be))
            return;
        renderSafe(be, partialTicks, ms, bufferSource, light, overlay);
    }

    protected abstract void renderSafe(T be, float partialTicks, MatrixStack ms, VertexConsumerProvider bufferSource, int light, int overlay);

    public boolean isInvalid(T be) {
        return !be.hasWorld() || be.getCachedState().getBlock() == Blocks.AIR;
    }

    public boolean shouldCullItem(Vec3d itemPos, World level) {
        //TODO
        //        if (level instanceof PonderLevel)
        //            return false;

        WorldRenderer accessor = MinecraftClient.getInstance().worldRenderer;
        Frustum frustum = accessor.capturedFrustum != null ? accessor.capturedFrustum : accessor.frustum;

        Box itemBB = new Box(itemPos.x - 0.25, itemPos.y - 0.25, itemPos.z - 0.25, itemPos.x + 0.25, itemPos.y + 0.25, itemPos.z + 0.25);

        return !frustum.isVisible(itemBB);
    }

    //TODO
    //    @Override
    //    public @NotNull Box getRenderBoundingBox(@NotNull T blockEntity) {
    //        if (blockEntity instanceof CachedRenderBBBlockEntity cbe)
    //            return cbe.getRenderBoundingBox();
    //
    //        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
    //    }
}
