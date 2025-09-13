package com.zurrtum.create.client.foundation.render;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.WorldView;

/**
 * Taken from EntityRendererManager
 */
public class ShadowRenderHelper {

    private static final RenderLayer SHADOW_LAYER = RenderLayer.getEntityNoOutline(Identifier.of("textures/misc/shadow.png"));

    public static void renderShadow(MatrixStack matrixStack, VertexConsumerProvider buffer, float opacity, float radius) {
        MatrixStack.Entry entry = matrixStack.peek();
        VertexConsumer builder = buffer.getBuffer(SHADOW_LAYER);

        opacity /= 2;
        shadowVertex(entry, builder, opacity, -1 * radius, 0, -1 * radius, 0, 0);
        shadowVertex(entry, builder, opacity, -1 * radius, 0, 1 * radius, 0, 1);
        shadowVertex(entry, builder, opacity, 1 * radius, 0, 1 * radius, 1, 1);
        shadowVertex(entry, builder, opacity, 1 * radius, 0, -1 * radius, 1, 0);
    }

    public static void renderShadow(MatrixStack matrixStack, VertexConsumerProvider buffer, WorldView world, Vec3d pos, float opacity, float radius) {
        float f = radius;

        double d2 = pos.getX();
        double d0 = pos.getY();
        double d1 = pos.getZ();
        int i = MathHelper.floor(d2 - (double) f);
        int j = MathHelper.floor(d2 + (double) f);
        int k = MathHelper.floor(d0 - (double) f);
        int l = MathHelper.floor(d0);
        int i1 = MathHelper.floor(d1 - (double) f);
        int j1 = MathHelper.floor(d1 + (double) f);
        MatrixStack.Entry entry = matrixStack.peek();
        VertexConsumer builder = buffer.getBuffer(SHADOW_LAYER);

        for (BlockPos blockpos : BlockPos.iterate(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
            renderBlockShadow(entry, builder, world, blockpos, d2, d0, d1, f, opacity);
        }
    }

    private static void renderBlockShadow(
        MatrixStack.Entry entry,
        VertexConsumer builder,
        WorldView world,
        BlockPos pos,
        double x,
        double y,
        double z,
        float radius,
        float opacity
    ) {
        BlockPos blockpos = pos.down();
        BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.getRenderType() != BlockRenderType.INVISIBLE && world.getLightLevel(pos) > 3) {
            if (blockstate.isFullCube(world, blockpos)) {
                VoxelShape voxelshape = blockstate.getOutlineShape(world, pos.down());
                if (!voxelshape.isEmpty()) {
                    float brightness = LightmapTextureManager.getBrightness(world.getDimension(), world.getLightLevel(pos));
                    float f = (float) ((opacity - (y - pos.getY()) / 2.0D) * 0.5D * brightness);
                    if (f >= 0.0F) {
                        if (f > 1.0F) {
                            f = 1.0F;
                        }

                        Box AABB = voxelshape.getBoundingBox();
                        double d0 = (double) pos.getX() + AABB.minX;
                        double d1 = (double) pos.getX() + AABB.maxX;
                        double d2 = (double) pos.getY() + AABB.minY;
                        double d3 = (double) pos.getZ() + AABB.minZ;
                        double d4 = (double) pos.getZ() + AABB.maxZ;
                        float f1 = (float) (d0 - x);
                        float f2 = (float) (d1 - x);
                        float f3 = (float) (d2 - y + 0.015625D);
                        float f4 = (float) (d3 - z);
                        float f5 = (float) (d4 - z);
                        float f6 = -f1 / 2.0F / radius + 0.5F;
                        float f7 = -f2 / 2.0F / radius + 0.5F;
                        float f8 = -f4 / 2.0F / radius + 0.5F;
                        float f9 = -f5 / 2.0F / radius + 0.5F;
                        shadowVertex(entry, builder, f, f1, f3, f4, f6, f8);
                        shadowVertex(entry, builder, f, f1, f3, f5, f6, f9);
                        shadowVertex(entry, builder, f, f2, f3, f5, f7, f9);
                        shadowVertex(entry, builder, f, f2, f3, f4, f7, f8);
                    }
                }
            }
        }
    }

    private static void shadowVertex(MatrixStack.Entry entry, VertexConsumer builder, float alpha, float x, float y, float z, float u, float v) {
        builder.vertex(entry.getPositionMatrix(), x, y, z).color(1.0F, 1.0F, 1.0F, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV)
            .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(entry.copy(), 0.0F, 1.0F, 0.0F);
    }

}
