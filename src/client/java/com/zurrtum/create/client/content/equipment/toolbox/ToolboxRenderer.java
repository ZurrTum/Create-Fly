package com.zurrtum.create.client.content.equipment.toolbox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ToolboxRenderer implements BlockEntityRenderer<ToolboxBlockEntity, ToolboxRenderer.ToolboxRenderState> {
    public ToolboxRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ToolboxRenderState createRenderState() {
        return new ToolboxRenderState();
    }

    @Override
    public void extractRenderState(
        ToolboxBlockEntity be,
        ToolboxRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderTypes.cutoutMovingBlock();
        state.lid = CachedBuffers.partial(AllPartialModels.TOOLBOX_LIDS.get(be.getColor()), state.blockState);
        state.drawer = CachedBuffers.partial(AllPartialModels.TOOLBOX_DRAWER, state.blockState);
        Direction facing = state.blockState.getValue(ToolboxBlock.FACING).getOpposite();
        state.yRot = Mth.DEG_TO_RAD * -facing.toYRot();
        state.xRot = Mth.DEG_TO_RAD * 135 * be.lid.getValue(tickProgress);
        float drawerOffset = be.drawers.getValue(tickProgress);
        state.offset1 = -drawerOffset * .175f;
        state.offset2 = state.offset1 * 2;
    }

    @Override
    public void submit(ToolboxRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static class ToolboxRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer lid;
        public SuperByteBuffer drawer;
        public float yRot;
        public float xRot;
        public float offset1;
        public float offset2;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            lid.center().rotateY(yRot).uncenter().translate(0, 0.375f, 0.75f).rotateX(xRot).translate(0, -0.375f, -0.75f).light(lightCoords)
                .renderInto(matricesEntry, vertexConsumer);
            drawer.center().rotateY(yRot).uncenter().translate(0, 0.125f, offset1).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
            drawer.center().rotateY(yRot).uncenter().translate(0, 0, offset2).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
