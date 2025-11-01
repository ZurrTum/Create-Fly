package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.*;

public class FluidRenderHelper {

    public static VertexConsumer getFluidBuilder(VertexConsumerProvider buffer) {
        return buffer.getBuffer(PonderRenderTypes.fluid());
    }

    public static void renderFluidBox(
        Fluid fluid,
        ComponentChanges changes,
        float xMin,
        float yMin,
        float zMin,
        float xMax,
        float yMax,
        float zMax,
        VertexConsumerProvider buffer,
        MatrixStack ms,
        int light,
        boolean renderBottom,
        boolean invertGasses
    ) {
        renderFluidBox(fluid, changes, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom, invertGasses);
    }

    public static void renderFluidBox(
        FluidStack stack,
        float xMin,
        float yMin,
        float zMin,
        float xMax,
        float yMax,
        float zMax,
        VertexConsumerProvider buffer,
        MatrixStack ms,
        int light,
        boolean renderBottom,
        boolean invertGasses
    ) {
        renderFluidBox(
            stack.getFluid(),
            stack.getComponentChanges(),
            xMin,
            yMin,
            zMin,
            xMax,
            yMax,
            zMax,
            getFluidBuilder(buffer),
            ms,
            light,
            renderBottom,
            invertGasses
        );
    }

    public static void renderFluidBox(
        Fluid fluid,
        ComponentChanges changes,
        float xMin,
        float yMin,
        float zMin,
        float xMax,
        float yMax,
        float zMax,
        VertexConsumer builder,
        MatrixStack ms,
        int light,
        boolean renderBottom,
        boolean invertGasses
    ) {
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        Sprite fluidTexture = config.still().get();

        int color = config.tint().apply(changes) | 0xff000000;
        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluid.getDefaultState().getBlockState().getLuminance());
        light = (light & 0xF00000) | luminosity << 4;

        Vec3d center = new Vec3d(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
        ms.push();
        //TODO
        if (invertGasses && false) {
            ms.translate(center.x, center.y, center.z);
            ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            ms.translate(-center.x, -center.y, -center.z);
        }

        MatrixStack.Entry entry = ms.peek();
        for (Direction side : Iterate.directions) {
            if (side == Direction.DOWN && !renderBottom)
                continue;

            boolean positive = side.getDirection() == Direction.AxisDirection.POSITIVE;
            if (side.getAxis().isHorizontal()) {
                if (side.getAxis() == Direction.Axis.X) {
                    renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin, builder, entry, light, color, fluidTexture);
                } else {
                    renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin, builder, entry, light, color, fluidTexture);
                }
            } else {
                renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin, builder, entry, light, color, fluidTexture);
            }
        }

        ms.pop();
    }

    public static void renderFluidBox(
        Fluid fluid,
        ComponentChanges changes,
        float xMin,
        float yMin,
        float zMin,
        float xMax,
        float yMax,
        float zMax,
        VertexConsumer builder,
        MatrixStack.Entry entry,
        int light,
        boolean renderBottom,
        boolean invertGasses
    ) {
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        Sprite fluidTexture = config.still().get();

        int color = config.tint().apply(changes) | 0xff000000;
        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluid.getDefaultState().getBlockState().getLuminance());
        light = (light & 0xF00000) | luminosity << 4;

        Vec3d center = new Vec3d(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
        //TODO
        if (invertGasses && false) {
            entry.translate((float) center.x, (float) center.y, (float) center.z);
            entry.rotate(RotationAxis.POSITIVE_X.rotationDegrees(180));
            entry.translate((float) -center.x, (float) -center.y, (float) -center.z);
        }

        for (Direction side : Iterate.directions) {
            if (side == Direction.DOWN && !renderBottom)
                continue;

            boolean positive = side.getDirection() == Direction.AxisDirection.POSITIVE;
            if (side.getAxis().isHorizontal()) {
                if (side.getAxis() == Direction.Axis.X) {
                    renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin, builder, entry, light, color, fluidTexture);
                } else {
                    renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin, builder, entry, light, color, fluidTexture);
                }
            } else {
                renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin, builder, entry, light, color, fluidTexture);
            }
        }
    }

    public static void renderStillTiledFace(
        Direction dir,
        float left,
        float down,
        float right,
        float up,
        float depth,
        VertexConsumer builder,
        MatrixStack.Entry entry,
        int light,
        int color,
        Sprite texture
    ) {
        renderTiledFace(dir, left, down, right, up, depth, builder, entry, light, color, texture, 1);
    }

    public static void renderTiledFace(
        Direction dir,
        float left,
        float down,
        float right,
        float up,
        float depth,
        VertexConsumer builder,
        MatrixStack.Entry entry,
        int light,
        int color,
        Sprite texture,
        float textureScale
    ) {
        boolean positive = dir.getDirection() == Direction.AxisDirection.POSITIVE;
        boolean horizontal = dir.getAxis().isHorizontal();
        boolean x = dir.getAxis() == Direction.Axis.X;

        float shrink = texture.getUvScaleDelta() * 0.25f * textureScale;
        float centerU = texture.getMinU() + (texture.getMaxU() - texture.getMinU()) * 0.5f * textureScale;
        float centerV = texture.getMinV() + (texture.getMaxV() - texture.getMinV()) * 0.5f * textureScale;

        float f;
        float x2;
        float y2;
        float u1, u2;
        float v1, v2;
        for (float x1 = left; x1 < right; x1 = x2) {
            f = MathHelper.floor(x1);
            x2 = Math.min(f + 1, right);
            if (dir == Direction.NORTH || dir == Direction.EAST) {
                f = MathHelper.ceil(x2);
                u1 = texture.getFrameU((f - x2) * textureScale);
                u2 = texture.getFrameU((f - x1) * textureScale);
            } else {
                u1 = texture.getFrameU((x1 - f) * textureScale);
                u2 = texture.getFrameU((x2 - f) * textureScale);
            }
            u1 = MathHelper.lerp(shrink, u1, centerU);
            u2 = MathHelper.lerp(shrink, u2, centerU);
            for (float y1 = down; y1 < up; y1 = y2) {
                f = MathHelper.floor(y1);
                y2 = Math.min(f + 1, up);
                if (dir == Direction.UP) {
                    v1 = texture.getFrameV((y1 - f) * textureScale);
                    v2 = texture.getFrameV((y2 - f) * textureScale);
                } else {
                    f = MathHelper.ceil(y2);
                    v1 = texture.getFrameV((f - y2) * textureScale);
                    v2 = texture.getFrameV((f - y1) * textureScale);
                }
                v1 = MathHelper.lerp(shrink, v1, centerV);
                v2 = MathHelper.lerp(shrink, v2, centerV);

                if (horizontal) {
                    if (x) {
                        putVertex(builder, entry, depth, y2, positive ? x2 : x1, color, u1, v1, dir, light);
                        putVertex(builder, entry, depth, y1, positive ? x2 : x1, color, u1, v2, dir, light);
                        putVertex(builder, entry, depth, y1, positive ? x1 : x2, color, u2, v2, dir, light);
                        putVertex(builder, entry, depth, y2, positive ? x1 : x2, color, u2, v1, dir, light);
                    } else {
                        putVertex(builder, entry, positive ? x1 : x2, y2, depth, color, u1, v1, dir, light);
                        putVertex(builder, entry, positive ? x1 : x2, y1, depth, color, u1, v2, dir, light);
                        putVertex(builder, entry, positive ? x2 : x1, y1, depth, color, u2, v2, dir, light);
                        putVertex(builder, entry, positive ? x2 : x1, y2, depth, color, u2, v1, dir, light);
                    }
                } else {
                    putVertex(builder, entry, x1, depth, positive ? y1 : y2, color, u1, v1, dir, light);
                    putVertex(builder, entry, x1, depth, positive ? y2 : y1, color, u1, v2, dir, light);
                    putVertex(builder, entry, x2, depth, positive ? y2 : y1, color, u2, v2, dir, light);
                    putVertex(builder, entry, x2, depth, positive ? y1 : y2, color, u2, v1, dir, light);
                }
            }
        }
    }

    protected static void putVertex(
        VertexConsumer builder,
        MatrixStack.Entry entry,
        float x,
        float y,
        float z,
        int color,
        float u,
        float v,
        Direction face,
        int light
    ) {

        Vec3i normal = face.getVector();
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;

        builder.vertex(entry.getPositionMatrix(), x, y, z).color(r, g, b, a).texture(u, v)
            //.overlayCoords(OverlayTexture.NO_OVERLAY)
            .light(light).normal(entry, normal.getX(), normal.getY(), normal.getZ());
    }

}