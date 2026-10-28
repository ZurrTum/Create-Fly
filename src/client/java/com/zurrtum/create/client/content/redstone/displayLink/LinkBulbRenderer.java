package com.zurrtum.create.client.content.redstone.displayLink;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.redstone.displayLink.LinkBulbRenderer.LinkBulbRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class LinkBulbRenderer implements BlockEntityRenderer<LinkWithBulbBlockEntity, LinkBulbRenderState> {
    public LinkBulbRenderer(Context context) {
    }

    @Override
    public LinkBulbRenderState createRenderState() {
        return new LinkBulbRenderState();
    }

    @Override
    public void extractRenderState(
        LinkWithBulbBlockEntity be,
        LinkBulbRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        state.breakProgress = crumblingOverlay;
        Direction face = be.getBulbFacing(state.blockState);
        state.yRadians = Radians.horizontalPlus180(face);
        state.xRadians = Radians.negatedVerticalPlus90(face);
        state.offset = be.getBulbOffset(state.blockState);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(be.getLevel());
        state.tube = CachedBuffers.partial(AllPartialModels.DISPLAY_LINK_TUBE, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        float glow = be.getGlow(tickProgress);
        if (glow < 0.125f) {
            return;
        }
        glow = (float) (1 - 2 * Math.pow(glow - 0.75f, 2));
        glow = Mth.clamp(glow, -1, 1);
        int color = (int) (200 * glow);
        state.glow = CachedBuffers.partial(AllPartialModels.DISPLAY_LINK_GLOW, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).color(color, color, color, 255)
            .disableDiffuse().extractRenderState();
    }

    @Override
    public void submit(
        LinkBulbRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        Pose pose = matrices.last();
        if (state.yRadians != 0 || state.xRadians != 0) {
            pose.translate(0.5f, 0.5f, 0.5f);
            if (state.yRadians != 0) {
                pose.rotate(Axis.YP, state.yRadians);
            }
            if (state.xRadians != 0) {
                pose.rotate(Axis.XP, state.xRadians);
            }
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
        Vec3 offset = state.offset;
        pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
        state.tube.submit(matrices, queue);
        if (state.glow != null) {
            state.glow.submit(CreateRenderTypes.additive(), matrices, queue.order(1));
        }
    }

    public static class LinkBulbRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState tube;
        public @Nullable SuperByteBufferRenderState glow;
        public float yRadians;
        public float xRadians;
        public @UnknownNullability Vec3 offset;
    }
}
