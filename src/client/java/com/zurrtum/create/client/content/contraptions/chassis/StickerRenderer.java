package com.zurrtum.create.client.content.contraptions.chassis;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.chassis.StickerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class StickerRenderer implements BlockEntityRenderer<StickerBlockEntity, StickerRenderer.StickerRenderState> {
    public StickerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public StickerRenderState createRenderState() {
        return new StickerRenderState();
    }

    @Override
    public void extractRenderState(
        StickerBlockEntity be,
        StickerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        Level world = be.getLevel();
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderType.solid();
        state.head = CachedBuffers.partial(AllPartialModels.STICKER_HEAD, state.blockState);
        state.seed = be.hashCode();
        Direction facing = state.blockState.getValue(StickerBlock.FACING);
        state.yRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
        state.xRot = Mth.DEG_TO_RAD * (AngleHelper.verticalAngle(facing) + 90);
        float offset;
        if (!be.isVirtual() && world != Minecraft.getInstance().level) {
            offset = state.blockState.getValue(StickerBlock.EXTENDED) ? 1 : 0;
        } else {
            offset = be.piston.getValue(AnimationTickHolder.getPartialTicks(world));
        }
        state.offset = (offset * offset) * 4 / 16f;
    }

    @Override
    public void submit(StickerRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static class StickerRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer head;
        public int seed;
        public float yRot;
        public float xRot;
        public float offset;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            head.nudge(seed).center().rotateY(yRot).rotateX(xRot).uncenter().translate(0, offset, 0);
            head.light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
