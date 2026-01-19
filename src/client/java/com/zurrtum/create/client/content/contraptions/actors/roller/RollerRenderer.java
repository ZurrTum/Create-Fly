package com.zurrtum.create.client.content.contraptions.actors.roller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlock;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RollerRenderer extends SmartBlockEntityRenderer<RollerBlockEntity, RollerRenderer.RollerRenderState> {
    public RollerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public RollerRenderState createRenderState() {
        return new RollerRenderState();
    }

    @Override
    public void extractRenderState(
        RollerBlockEntity be,
        RollerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.layer = RenderTypes.cutoutMovingBlock();
        state.wheel = CachedBuffers.partial(AllPartialModels.ROLLER_WHEEL, state.blockState);
        Direction facing = state.blockState.getValue(RollerBlock.FACING);
        state.offset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(17 / 16f).add(0, -0.25f, 0);
        float angle = AngleHelper.horizontalAngle(facing);
        state.wheelAngle = AngleHelper.rad(angle);
        Level world = be.getLevel();
        float time = AnimationTickHolder.getRenderTime(world) / 20;
        state.rotate = AngleHelper.rad((time * be.getAnimatedSpeed()) % 360);
        state.yRot = Mth.DEG_TO_RAD * 90;
        state.frame = CachedBuffers.partial(AllPartialModels.ROLLER_FRAME, state.blockState);
        state.frameAngle = AngleHelper.rad(angle + 180);
    }

    @Override
    public void submit(RollerRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static class RollerRenderState extends SmartRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer wheel;
        public Vec3 offset;
        public float wheelAngle;
        public float rotate;
        public float yRot;
        public SuperByteBuffer frame;
        public float frameAngle;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            wheel.translate(offset).rotateCentered(wheelAngle, Direction.UP).rotate(rotate, Direction.WEST).translate(0, -.5, .5).rotateY(yRot)
                .light(lightCoords).renderInto(matricesEntry, vertexConsumer);
            frame.rotateCentered(frameAngle, Direction.UP).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
