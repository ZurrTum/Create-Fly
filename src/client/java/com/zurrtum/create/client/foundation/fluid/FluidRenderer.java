package com.zurrtum.create.client.foundation.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.QuadRenderHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public class FluidRenderer {
    public static FluidStreamRenderState extractFluidStreamRenderState(
        @Nullable BlockAndTintGetter level,
        @Nullable BlockPos pos,
        FluidStateModelSet fluidStateModelSet,
        Fluid fluid,
        DataComponentPatch components,
        Direction side,
        float progress,
        boolean inbound,
        float radius,
        int lightCoords
    ) {
        FluidState fluidState = fluid.defaultFluidState();
        BlockState blockState = fluidState.createLegacyBlock();
        FluidModel model = fluidStateModelSet.get(fluidState);
        int tint = AllFluidConfigs.getTint(level, pos, blockState, model, fluid, components) | 0xff000000;
        TextureAtlasSprite flowTexture = model.flowingMaterial().sprite();
        TextureAtlasSprite stillTexture = progress != 1 ? model.stillMaterial().sprite() : null;
        int lightEmission = blockState.getLightEmission();
        RenderType layer = switch (model.layer()) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
        QuadRenderHelper.markSpriteActive(flowTexture);
        return new FluidStreamRenderState(
            layer,
            flowTexture,
            stillTexture,
            tint,
            lightEmission,
            side,
            progress,
            inbound,
            radius,
            lightCoords
        );
    }

    public static void renderFluidStream(
        TextureAtlasSprite flowTexture,
        @Nullable TextureAtlasSprite stillTexture,
        int tint,
        int lightEmission,
        Direction direction,
        float radius,
        float progress,
        boolean inbound,
        VertexConsumer builder,
        PoseStack.Pose entry,
        int light
    ) {
        int blockLightIn = light >> 4 & 0xF;
        int luminosity = Math.max(blockLightIn, lightEmission);
        light = light & 0xF00000 | luminosity << 4;

        if (inbound) {
            direction = direction.getOpposite();
        }

        entry = entry.copy();
        entry.translate(0.5f, 0.5f, 0.5f);
        float yRadians = Radians.horizontal(direction);
        if (yRadians != 0) {
            entry.rotate(Axis.YP, yRadians);
        }
        if (direction == Direction.UP) {
            entry.rotate(Axis.XP, Radians.DEG_180);
        } else if (direction != Direction.DOWN) {
            entry.rotate(Axis.XP, Radians.DEG_270);
        }
        entry.translate(0, -0.5f, 0);

        float hMin = -radius;
        float y = inbound ? 1 : 0.5f;
        float yMin = y - Mth.clamp(progress * 0.5f, 0, 1);

        for (int i = 0; i < 4; i++) {
            renderFlowingTiledFace(
                Direction.SOUTH,
                hMin,
                yMin,
                radius,
                y,
                radius,
                builder,
                entry,
                light,
                tint,
                flowTexture
            );
            entry.rotate(Axis.YP, Radians.DEG_90);
        }

        if (stillTexture != null) {
            FluidRenderHelper.renderTiledFace(
                Direction.DOWN,
                hMin,
                hMin,
                radius,
                radius,
                yMin,
                builder,
                entry,
                light,
                tint,
                stillTexture,
                1
            );
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
        FluidRenderHelper.renderTiledFace(
            dir,
            left,
            down,
            right,
            up,
            depth,
            builder,
            entry,
            light,
            color,
            texture,
            0.5f
        );
    }

    public record FluidStreamRenderState(RenderType layer, TextureAtlasSprite flowTexture,
                                         @Nullable TextureAtlasSprite stillTexture, int tint, int lightEmission,
                                         Direction side, float value, boolean inbound, float radius,
                                         int lightCoords) implements SubmitNodeCollector.CustomGeometryRenderer {
        public void submit(SubmitNodeCollector queue, PoseStack poseStack) {
            queue.submitCustomGeometry(poseStack, layer, this);
        }

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer vertexConsumer) {
            renderFluidStream(
                flowTexture,
                stillTexture,
                tint,
                lightEmission,
                side,
                radius,
                value,
                inbound,
                vertexConsumer,
                pose,
                lightCoords
            );
        }
    }
}
