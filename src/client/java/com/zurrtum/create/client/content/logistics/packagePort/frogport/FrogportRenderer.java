package com.zurrtum.create.client.content.logistics.packagePort.frogport;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.NameplateRenderState;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FrogportRenderer implements BlockEntityRenderer<FrogportBlockEntity, FrogportRenderer.FrogportRenderState> {
    public FrogportRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public FrogportRenderState createRenderState() {
        return new FrogportRenderState();
    }

    @Override
    public void updateRenderState(
        FrogportBlockEntity be,
        FrogportRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        World world = be.getWorld();
        String filter = be.addressFilter;
        boolean support = VisualizationManager.supportsVisualization(world);
        boolean name = filter != null && !filter.isBlank();
        if (support && !name) {
            return;
        }
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        if (name) {
            state.name = SmartBlockEntityRenderer.getNameplateRenderState(
                be,
                state.pos,
                cameraPos,
                Text.literal(filter),
                1,
                state.lightmapCoordinates
            );
        }
        if (support) {
            return;
        }
        FrogportRenderData data = state.data = new FrogportRenderData();
        data.layer = RenderLayer.getCutoutMipped();
        data.body = CachedBuffers.partial(AllPartialModels.FROGPORT_BODY, state.blockState);
        Vec3d diff;
        float tongueLength, headPitch, headPitchModifier;
        boolean animating = be.isAnimationInProgress();
        boolean depositing = be.currentlyDepositing;
        if (be.target != null) {
            diff = be.target.getExactTargetLocation(be, world, state.pos).subtract(0, animating && depositing ? 0 : 0.75, 0)
                .subtract(Vec3d.ofCenter(state.pos));
            float tonguePitch = (float) MathHelper.atan2(diff.y, diff.multiply(1, 0, 1).length() + (3 / 16f)) * MathHelper.DEGREES_PER_RADIAN;
            tongueLength = Math.max((float) diff.length(), 1);
            headPitch = MathHelper.clamp(tonguePitch * 2, 60, 100);
            data.tonguePitch = MathHelper.RADIANS_PER_DEGREE * tonguePitch;
        } else {
            diff = Vec3d.ZERO;
            tongueLength = 0;
            headPitch = 80;
        }
        if (animating) {
            float progress = be.animationProgress.getValue(tickProgress);
            float scale, itemDistance;
            if (depositing) {
                double modifier = Math.max(0, 1 - Math.pow((progress - 0.25) * 4 - 1, 4));
                itemDistance = (float) Math.max(tongueLength * Math.min(1, (progress - 0.25) * 3), tongueLength * modifier);
                tongueLength *= (float) Math.max(0, 1 - Math.pow((progress * 1.25 - 0.25) * 4 - 1, 4));
                headPitchModifier = (float) Math.max(0, 1 - Math.pow((progress * 1.25) * 2 - 1, 4));
                scale = 0.25f + progress * 3 / 4;

            } else {
                tongueLength *= (float) Math.pow(Math.max(0, 1 - progress * 1.25), 5);
                headPitchModifier = 1 - (float) Math.min(1, Math.max(0, (Math.pow(progress * 1.5, 2) - 0.5) * 2));
                scale = (float) Math.max(0.5, 1 - progress * 1.25);
                itemDistance = tongueLength;
            }
            if (be.animatedPackage != null && scale >= 0.45) {
                Identifier key = Registries.ITEM.getId(be.animatedPackage.getItem());
                if (key != Registries.ITEM.getDefaultId()) {
                    data.box = CachedBuffers.partial(AllPartialModels.PACKAGES.get(key), state.blockState);
                    data.boxOffset = diff.normalize().multiply(itemDistance).subtract(0, depositing ? 0.75 : 0, 0);
                    data.boxScale = scale;
                    if (depositing) {
                        data.rig = CachedBuffers.partial(AllPartialModels.PACKAGE_RIGGING.get(key), state.blockState);
                    }
                }
            }
        } else {
            tongueLength = 0;
            float anticipation = be.anticipationProgress.getValue(tickProgress);
            headPitchModifier = anticipation > 0 ? (float) Math.max(0, 1 - Math.pow((anticipation * 1.25) * 2 - 1, 4)) : 0;
        }
        headPitch *= headPitchModifier;
        float openProgress = be.manualOpenAnimationProgress.getValue(tickProgress);
        data.headPitch = MathHelper.RADIANS_PER_DEGREE * Math.max(headPitch, openProgress * 60);
        tongueLength = Math.max(tongueLength, openProgress * 0.25f);
        data.yRot = MathHelper.RADIANS_PER_DEGREE * be.getYaw();
        data.head = CachedBuffers.partial(be.goggles ? AllPartialModels.FROGPORT_HEAD_GOGGLES : AllPartialModels.FROGPORT_HEAD, state.blockState);
        data.tongue = CachedBuffers.partial(AllPartialModels.FROGPORT_TONGUE, state.blockState);
        data.tongueScale = tongueLength / (7 / 16f);
        data.light = state.lightmapCoordinates;
    }

    @Override
    public void render(FrogportRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.name != null) {
            state.name.render(matrices, queue, cameraState);
        }
        if (state.data != null) {
            queue.submitCustom(matrices, state.data.layer, state.data);
        }
    }

    public static class FrogportRenderState extends BlockEntityRenderState {
        public NameplateRenderState name;
        public FrogportRenderData data;
    }

    public static class FrogportRenderData implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer body;
        public float tonguePitch;
        public float yRot;
        public SuperByteBuffer head;
        public float headPitch;
        public SuperByteBuffer tongue;
        public float tongueScale;
        public SuperByteBuffer rig;
        public SuperByteBuffer box;
        public Vec3d boxOffset;
        public float boxScale;
        public int light;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            body.center().rotateY(yRot).uncenter().light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
            head.center().rotateY(yRot).uncenter().translate(0.5f, 0.625f, 0.6875f).rotateX(headPitch).translate(-0.5f, -0.625f, -0.6875f)
                .light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
            tongue.center().rotateY(yRot).uncenter().translate(0.5f, 0.625f, 0.6875f).rotateX(tonguePitch).scale(1, 1, tongueScale)
                .translate(-0.5f, -0.625f, -0.6875f).light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
            if (box != null) {
                box.translate(0, 0.1875f, 0).translate(boxOffset).center().scale(boxScale).uncenter().light(light).overlay(OverlayTexture.DEFAULT_UV)
                    .renderInto(matricesEntry, vertexConsumer);
            }
            if (rig != null) {
                rig.translate(0, 0.1875f, 0).translate(boxOffset).center().scale(boxScale).uncenter().light(light).overlay(OverlayTexture.DEFAULT_UV)
                    .renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
