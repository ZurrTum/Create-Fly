package com.zurrtum.create.client.content.contraptions.actors.harvester;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlock;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HarvesterRenderer implements BlockEntityRenderer<HarvesterBlockEntity, HarvesterRenderer.HarvesterRenderState> {
    public static final Vec3 PIVOT = new Vec3(0, 6, 9);

    public HarvesterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public HarvesterRenderState createRenderState() {
        return new HarvesterRenderState();
    }

    @Override
    public void extractRenderState(
        HarvesterBlockEntity be,
        HarvesterRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderType.cutoutMipped();
        state.model = CachedBuffers.partial(AllPartialModels.HARVESTER_BLADE, state.blockState);
        float originOffset = 1 / 16f;
        state.rotOffset = new Vec3(0, PIVOT.y * originOffset, PIVOT.z * originOffset);
        float time = AnimationTickHolder.getRenderTime(be.getLevel()) / 20;
        state.angle = AngleHelper.rad((time * be.getAnimatedSpeed()) % 360);
        state.horizontalAngle = AngleHelper.rad(AngleHelper.horizontalAngle(state.blockState.getValue(HarvesterBlock.FACING)));
    }

    @Override
    public void submit(HarvesterRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static class HarvesterRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer model;
        public float angle;
        public Vec3 rotOffset;
        public float horizontalAngle;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.rotateCentered(horizontalAngle, Direction.UP);
            model.translate(rotOffset.x, rotOffset.y, rotOffset.z);
            model.rotate(angle, Direction.WEST);
            model.translate(-rotOffset.x, -rotOffset.y, -rotOffset.z);
            model.light(lightCoords);
            model.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
