package com.zurrtum.create.client.content.decoration.steamWhistle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.WhistleAnimationBehaviour;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WhistleRenderer implements BlockEntityRenderer<WhistleBlockEntity, WhistleRenderer.WhistleRenderState> {
    public WhistleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public WhistleRenderState createRenderState() {
        return new WhistleRenderState();
    }

    @Override
    public void extractRenderState(
        WhistleBlockEntity be,
        WhistleRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        state.blockState = be.getBlockState();
        if (!(state.blockState.getBlock() instanceof WhistleBlock)) {
            return;
        }
        state.blockPos = be.getBlockPos();
        state.blockEntityType = be.getType();
        Level world = be.getLevel();
        state.lightCoords = world != null ? LevelRenderer.getLightColor(world, state.blockPos) : LightTexture.FULL_BRIGHT;
        state.breakProgress = crumblingOverlay;
        state.layer = RenderTypes.solidMovingBlock();
        Direction direction = state.blockState.getValue(WhistleBlock.FACING);
        WhistleSize size = state.blockState.getValue(WhistleBlock.SIZE);
        PartialModel mouth = size == WhistleSize.LARGE ? AllPartialModels.WHISTLE_MOUTH_LARGE : size == WhistleSize.MEDIUM ? AllPartialModels.WHISTLE_MOUTH_MEDIUM : AllPartialModels.WHISTLE_MOUTH_SMALL;
        WhistleAnimationBehaviour behaviour = (WhistleAnimationBehaviour) be.getBehaviour(AnimationBehaviour.TYPE);
        if (behaviour != null) {
            float offset = behaviour.animation.getValue(tickProgress);
            if (behaviour.animation.getChaseTarget() > 0 && behaviour.animation.getValue() > 0.5f) {
                float wiggleProgress = (AnimationTickHolder.getTicks(world) + tickProgress) / 8f;
                offset = (float) (offset - Math.sin(wiggleProgress * (2 * Mth.PI) * (4 - size.ordinal())) / 16f);
            }
            state.offset = offset * 4 / 16f;
        }
        state.model = CachedBuffers.partial(mouth, state.blockState);
        state.yRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(direction);
    }

    @Override
    public void submit(WhistleRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static class WhistleRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer model;
        public float yRot;
        public float offset;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.center().rotateY(yRot).uncenter().translate(0, offset, 0).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
