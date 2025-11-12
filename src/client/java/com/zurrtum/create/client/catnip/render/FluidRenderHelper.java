package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public class FluidRenderHelper {

    public static VertexConsumer getFluidBuilder(MultiBufferSource buffer) {
        return buffer.getBuffer(PonderRenderTypes.fluid());
    }

    public static void renderFluidBox(
        Fluid fluid,
        DataComponentPatch changes,
        float xMin,
        float yMin,
        float zMin,
        float xMax,
        float yMax,
        float zMax,
        MultiBufferSource buffer,
        PoseStack ms,
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
        MultiBufferSource buffer,
        PoseStack ms,
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
        DataComponentPatch changes,
        float xMin,
        float yMin,
        float zMin,
        float xMax,
        float yMax,
        float zMax,
        VertexConsumer builder,
        PoseStack ms,
        int light,
        boolean renderBottom,
        boolean invertGasses
    ) {
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        TextureAtlasSprite fluidTexture = config.still().get();

        int color = config.tint().apply(changes) | 0xff000000;
        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluid.defaultFluidState().createLegacyBlock().getLightEmission());
        light = (light & 0xF00000) | luminosity << 4;

        Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
        ms.pushPose();
        //TODO
        if (invertGasses && false) {
            ms.translate(center.x, center.y, center.z);
            ms.mulPose(Axis.XP.rotationDegrees(180));
            ms.translate(-center.x, -center.y, -center.z);
        }

        PoseStack.Pose entry = ms.last();
        for (Direction side : Iterate.directions) {
            if (side == Direction.DOWN && !renderBottom)
                continue;

            boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
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

        ms.popPose();
    }

    public static void renderFluidBox(
        Fluid fluid,
        DataComponentPatch changes,
        float xMin,
        float yMin,
        float zMin,
        float xMax,
        float yMax,
        float zMax,
        VertexConsumer builder,
        PoseStack.Pose entry,
        int light,
        boolean renderBottom,
        boolean invertGasses
    ) {
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        TextureAtlasSprite fluidTexture = config.still().get();

        int color = config.tint().apply(changes) | 0xff000000;
        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluid.defaultFluidState().createLegacyBlock().getLightEmission());
        light = (light & 0xF00000) | luminosity << 4;

        Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
        //TODO
        if (invertGasses && false) {
            entry.translate((float) center.x, (float) center.y, (float) center.z);
            entry.rotate(Axis.XP.rotationDegrees(180));
            entry.translate((float) -center.x, (float) -center.y, (float) -center.z);
        }

        for (Direction side : Iterate.directions) {
            if (side == Direction.DOWN && !renderBottom)
                continue;

            boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
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
        PoseStack.Pose entry,
        int light,
        int color,
        TextureAtlasSprite texture
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
        PoseStack.Pose entry,
        int light,
        int color,
        TextureAtlasSprite texture,
        float textureScale
    ) {
        boolean positive = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        boolean horizontal = dir.getAxis().isHorizontal();
        boolean x = dir.getAxis() == Direction.Axis.X;

        float shrink = texture.uvShrinkRatio() * 0.25f * textureScale;
        float centerU = texture.getU0() + (texture.getU1() - texture.getU0()) * 0.5f * textureScale;
        float centerV = texture.getV0() + (texture.getV1() - texture.getV0()) * 0.5f * textureScale;

        float f;
        float x2;
        float y2;
        float u1, u2;
        float v1, v2;
        for (float x1 = left; x1 < right; x1 = x2) {
            f = Mth.floor(x1);
            x2 = Math.min(f + 1, right);
            if (dir == Direction.NORTH || dir == Direction.EAST) {
                f = Mth.ceil(x2);
                u1 = texture.getU((f - x2) * textureScale);
                u2 = texture.getU((f - x1) * textureScale);
            } else {
                u1 = texture.getU((x1 - f) * textureScale);
                u2 = texture.getU((x2 - f) * textureScale);
            }
            u1 = Mth.lerp(shrink, u1, centerU);
            u2 = Mth.lerp(shrink, u2, centerU);
            for (float y1 = down; y1 < up; y1 = y2) {
                f = Mth.floor(y1);
                y2 = Math.min(f + 1, up);
                if (dir == Direction.UP) {
                    v1 = texture.getV((y1 - f) * textureScale);
                    v2 = texture.getV((y2 - f) * textureScale);
                } else {
                    f = Mth.ceil(y2);
                    v1 = texture.getV((f - y2) * textureScale);
                    v2 = texture.getV((f - y1) * textureScale);
                }
                v1 = Mth.lerp(shrink, v1, centerV);
                v2 = Mth.lerp(shrink, v2, centerV);

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
        PoseStack.Pose entry,
        float x,
        float y,
        float z,
        int color,
        float u,
        float v,
        Direction face,
        int light
    ) {

        Vec3i normal = face.getUnitVec3i();
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;

        builder.addVertex(entry.pose(), x, y, z).setColor(r, g, b, a).setUv(u, v)
            //.overlayCoords(OverlayTexture.NO_OVERLAY)
            .setLight(light).setNormal(entry, normal.getX(), normal.getY(), normal.getZ());
    }

}