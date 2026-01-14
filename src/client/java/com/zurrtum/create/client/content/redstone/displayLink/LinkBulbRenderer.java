package com.zurrtum.create.client.content.redstone.displayLink;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;
import com.zurrtum.create.client.foundation.render.RenderTypes;
import com.zurrtum.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class LinkBulbRenderer implements BlockEntityRenderer<LinkWithBulbBlockEntity, LinkBulbRenderer.LinkBulbRenderState> {
    private static final boolean IRIS = FabricLoader.getInstance().isModLoaded("iris");

    public LinkBulbRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public LinkBulbRenderState createRenderState() {
        return new LinkBulbRenderState();
    }

    @Override
    public void updateRenderState(
        LinkWithBulbBlockEntity be,
        LinkBulbRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        Direction face = be.getBulbFacing(state.blockState);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * (AngleHelper.horizontalAngle(face) + 180);
        state.xRot = MathHelper.RADIANS_PER_DEGREE * (-AngleHelper.verticalAngle(face) - 90);
        state.offset = be.getBulbOffset(state.blockState);
        state.tube = CachedBuffers.partial(AllPartialModels.DISPLAY_LINK_TUBE, state.blockState);
        float glow = be.getGlow(tickProgress);
        if (glow < .125f) {
            state.translucent = ShadersModHelper.isShaderPackInUse() ? RenderLayer.getTranslucentMovingBlock() : PonderRenderTypes.translucent();
            return;
        }
        state.translucent = ShadersModHelper.isShaderPackInUse() ? RenderLayer.getTranslucentMovingBlock() : RenderTypes.translucent();
        state.additive = RenderTypes.additive();
        state.glow = CachedBuffers.partial(AllPartialModels.DISPLAY_LINK_GLOW, state.blockState);
        glow = (float) (1 - (2 * Math.pow(glow - .75f, 2)));
        glow = MathHelper.clamp(glow, -1, 1);
        state.color = (int) (200 * glow);
    }

    @Override
    public void render(LinkBulbRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(state.yRot));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(state.xRot));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        queue.submitCustom(matrices, state.translucent, state::renderTube);
        if (state.glow != null) {
            (IRIS ? queue.getBatchingQueue(1) : queue).submitCustom(matrices, state.additive, state::renderGlow);
        }
    }

    public static class LinkBulbRenderState extends BlockEntityRenderState {
        public RenderLayer translucent;
        public RenderLayer additive;
        public SuperByteBuffer tube;
        public SuperByteBuffer glow;
        public float yRot;
        public float xRot;
        public Vec3d offset;
        public int color;

        public void renderTube(MatrixStack.Entry entry, VertexConsumer vertexConsumer) {
            tube.translate(offset).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).renderInto(entry, vertexConsumer);
        }

        public void renderGlow(MatrixStack.Entry entry, VertexConsumer vertexConsumer) {
            glow.translate(offset).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).color(color, color, color, 255).disableDiffuse()
                .renderInto(entry, vertexConsumer);
        }
    }
}
