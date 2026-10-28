package com.zurrtum.create.client.catnip.ghostblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

public abstract class GhostBlockRenderer {
    private static final GhostBlockRenderer STANDARD = new DefaultGhostBlockRenderer();
    private static final GhostBlockRenderer TRANSPARENT = new TransparentGhostBlockRenderer();

    public static GhostBlockRenderer standard() {
        return STANDARD;
    }

    public static GhostBlockRenderer transparent() {
        return TRANSPARENT;
    }

    public abstract void render(
        BlockStateModelSet blockStateModelSet,
        PoseStack ms,
        SubmitNodeCollector queue,
        Vec3 camera,
        GhostBlockParams params
    );

    private static class DefaultGhostBlockRenderer extends GhostBlockRenderer {
        @Override
        public void render(
            BlockStateModelSet blockStateModelSet,
            PoseStack ms,
            SubmitNodeCollector queue,
            Vec3 camera,
            GhostBlockParams params
        ) {
            BlockPos pos = params.pos;
            ms.pushPose();
            ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            CachedBuffers.block(params.state).light(LightCoordsUtil.FULL_BRIGHT).submit(ms, queue);
            ms.popPose();
        }
    }

    private static class TransparentGhostBlockRenderer extends GhostBlockRenderer {
        @Override
        public void render(
            BlockStateModelSet blockStateModelSet,
            PoseStack ms,
            SubmitNodeCollector queue,
            Vec3 camera,
            GhostBlockParams params
        ) {
            BlockPos pos = params.pos;
            ms.pushPose();
            Pose pose = ms.last();
            pose.translate(
                (float) (pos.getX() - camera.x),
                (float) (pos.getY() - camera.y),
                (float) (pos.getZ() - camera.z)
            );
            pose.pose().scaleAround(0.85f, 0.5f, 0.5f, 0.5f);
            int color = ARGB.white(params.alphaSupplier.get() * 0.75f * PlacementClient.getCurrentAlpha());
            CachedBuffers.block(params.state).color(color).light(LightCoordsUtil.FULL_BRIGHT)
                .submit(RenderTypes.translucentMovingBlock(), ms, queue);
            ms.popPose();
        }
    }
}
