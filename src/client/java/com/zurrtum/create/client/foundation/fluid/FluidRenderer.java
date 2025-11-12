package com.zurrtum.create.client.foundation.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;

public class FluidRenderer {
    public static void renderFluidStream(
        Fluid fluid,
        DataComponentPatch changes,
        Direction direction,
        float radius,
        float progress,
        boolean inbound,
        VertexConsumer builder,
        PoseStack.Pose entry,
        int light
    ) {
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        TextureAtlasSprite flowTexture = config.flowing().get();
        TextureAtlasSprite stillTexture = config.still().get();

        int color = config.tint().apply(changes) | 0xff000000;
        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluid.defaultFluidState().createLegacyBlock().getLightEmission());
        light = (light & 0xF00000) | luminosity << 4;

        if (inbound)
            direction = direction.getOpposite();

        entry = entry.copy();
        entry.translate(0.5f, 0.5f, 0.5f);
        entry.rotate(Axis.YP.rotation(Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(direction)));
        entry.rotate(Axis.XP.rotation(Mth.DEG_TO_RAD * (direction == Direction.UP ? 180 : direction == Direction.DOWN ? 0 : 270)));
        entry.translate(0, -0.5f, 0);

        float hMin = -radius;
        float y = inbound ? 1 : .5f;
        float yMin = y - Mth.clamp(progress * .5f, 0, 1);

        for (int i = 0; i < 4; i++) {
            renderFlowingTiledFace(Direction.SOUTH, hMin, yMin, radius, y, radius, builder, entry, light, color, flowTexture);
            entry.rotate(Axis.YP.rotation(Mth.DEG_TO_RAD * 90));
        }

        if (progress != 1) {
            FluidRenderHelper.renderStillTiledFace(Direction.DOWN, hMin, hMin, radius, radius, yMin, builder, entry, light, color, stillTexture);
        }
    }

    public static void renderFlowingTiledFace(
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
        FluidRenderHelper.renderTiledFace(dir, left, down, right, up, depth, builder, entry, light, color, texture, 0.5f);
    }
}
