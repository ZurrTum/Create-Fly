package com.zurrtum.create.client.content.redstone.displayLink;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LinkBulbRenderer implements BlockEntityRenderer<LinkWithBulbBlockEntity, LinkBulbRenderer.LinkBulbRenderState> {
    private static final boolean IRIS = FabricLoader.getInstance().isModLoaded("iris");

    public LinkBulbRenderer(BlockEntityRendererProvider.Context context) {
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
        state.lightCoords = be.getLevel() instanceof PonderLevel ? 0 : LightCoordsUtil.FULL_BRIGHT;
        state.breakProgress = crumblingOverlay;
        Direction face = be.getBulbFacing(state.blockState);
        state.yRot = Mth.DEG_TO_RAD * (AngleHelper.horizontalAngle(face) + 180);
        state.xRot = Mth.DEG_TO_RAD * (-AngleHelper.verticalAngle(face) - 90);
        state.offset = be.getBulbOffset(state.blockState);
        state.tube = CachedBuffers.partial(AllPartialModels.DISPLAY_LINK_TUBE, state.blockState);
        float glow = be.getGlow(tickProgress);
        if (glow < .125f) {
            state.translucent = RenderTypes.translucentMovingBlock();
            return;
        }
        state.translucent = CreateRenderTypes.translucent();
        state.additive = CreateRenderTypes.additive();
        state.glow = CachedBuffers.partial(AllPartialModels.DISPLAY_LINK_GLOW, state.blockState);
        glow = (float) (1 - (2 * Math.pow(glow - .75f, 2)));
        glow = Mth.clamp(glow, -1, 1);
        state.color = (int) (200 * glow);
    }

    @Override
    public void submit(LinkBulbRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.YP.rotation(state.yRot));
        matrices.mulPose(Axis.XP.rotation(state.xRot));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        queue.submitCustomGeometry(matrices, state.translucent, state::renderTube);
        if (state.glow != null) {
            (IRIS ? queue.order(1) : queue).submitCustomGeometry(matrices, state.additive, state::renderGlow);
        }
    }

    public static class LinkBulbRenderState extends BlockEntityRenderState {
        public RenderType translucent;
        public RenderType additive;
        public SuperByteBuffer tube;
        public @Nullable SuperByteBuffer glow;
        public float yRot;
        public float xRot;
        public Vec3 offset;
        public int color;

        public void renderTube(PoseStack.Pose entry, VertexConsumer vertexConsumer) {
            tube.translate(offset).light(lightCoords).renderInto(entry, vertexConsumer);
        }

        public void renderGlow(PoseStack.Pose entry, VertexConsumer vertexConsumer) {
            glow.translate(offset).light(lightCoords).color(color, color, color, 255).disableDiffuse().renderInto(entry, vertexConsumer);
        }
    }
}
