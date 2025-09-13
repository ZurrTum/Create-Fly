package com.zurrtum.create.client.content.logistics.packagePort.frogport;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FrogportRenderer extends SmartBlockEntityRenderer<FrogportBlockEntity> {

    public FrogportRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        FrogportBlockEntity blockEntity,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        SuperByteBuffer body = CachedBuffers.partial(AllPartialModels.FROGPORT_BODY, blockEntity.getCachedState());

        float yaw = blockEntity.getYaw();

        float headPitch = 80;
        float tonguePitch = 0;
        float tongueLength = 0;
        float headPitchModifier = 1;

        boolean hasTarget = blockEntity.target != null;
        boolean animating = blockEntity.isAnimationInProgress();
        boolean depositing = blockEntity.currentlyDepositing;

        Vec3d diff = Vec3d.ZERO;

        if (blockEntity.addressFilter != null && !blockEntity.addressFilter.isBlank()) {
            renderNameplateOnHover(blockEntity, Text.literal(blockEntity.addressFilter), 1, ms, buffer, light);
        }

        if (VisualizationManager.supportsVisualization(blockEntity.getWorld())) {
            return;
        }

        if (hasTarget) {
            diff = blockEntity.target.getExactTargetLocation(blockEntity, blockEntity.getWorld(), blockEntity.getPos())
                .subtract(0, animating && depositing ? 0 : 0.75, 0).subtract(Vec3d.ofCenter(blockEntity.getPos()));
            tonguePitch = (float) MathHelper.atan2(diff.y, diff.multiply(1, 0, 1).length() + (3 / 16f)) * MathHelper.DEGREES_PER_RADIAN;
            tongueLength = Math.max((float) diff.length(), 1);
            headPitch = MathHelper.clamp(tonguePitch * 2, 60, 100);
        }

        if (animating) {
            float progress = blockEntity.animationProgress.getValue(partialTicks);
            float scale = 1;
            float itemDistance = 0;

            if (depositing) {
                double modifier = Math.max(0, 1 - Math.pow((progress - 0.25) * 4 - 1, 4));
                itemDistance = (float) Math.max(tongueLength * Math.min(1, (progress - 0.25) * 3), tongueLength * modifier);
                tongueLength *= Math.max(0, 1 - Math.pow((progress * 1.25 - 0.25) * 4 - 1, 4));
                headPitchModifier = (float) Math.max(0, 1 - Math.pow((progress * 1.25) * 2 - 1, 4));
                scale = 0.25f + progress * 3 / 4;

            } else {
                tongueLength *= Math.pow(Math.max(0, 1 - progress * 1.25), 5);
                headPitchModifier = 1 - (float) Math.min(1, Math.max(0, (Math.pow(progress * 1.5, 2) - 0.5) * 2));
                scale = (float) Math.max(0.5, 1 - progress * 1.25);
                itemDistance = tongueLength;
            }

            renderPackage(blockEntity, ms, buffer, light, overlay, diff, scale, itemDistance);

        } else {
            tongueLength = 0;
            float anticipation = blockEntity.anticipationProgress.getValue(partialTicks);
            headPitchModifier = anticipation > 0 ? (float) Math.max(0, 1 - Math.pow((anticipation * 1.25) * 2 - 1, 4)) : 0;
        }

        headPitch *= headPitchModifier;

        headPitch = Math.max(headPitch, blockEntity.manualOpenAnimationProgress.getValue(partialTicks) * 60);
        tongueLength = Math.max(tongueLength, blockEntity.manualOpenAnimationProgress.getValue(partialTicks) * 0.25f);


        RenderLayer cutoutMipped = RenderLayer.getCutoutMipped();
        body.center().rotateYDegrees(yaw).uncenter().light(light).overlay(overlay).renderInto(ms, buffer.getBuffer(cutoutMipped));

        SuperByteBuffer head = CachedBuffers.partial(
            blockEntity.goggles ? AllPartialModels.FROGPORT_HEAD_GOGGLES : AllPartialModels.FROGPORT_HEAD,
            blockEntity.getCachedState()
        );

        head.center().rotateYDegrees(yaw).uncenter().translate(8 / 16f, 10 / 16f, 11 / 16f).rotateXDegrees(headPitch)
            .translateBack(8 / 16f, 10 / 16f, 11 / 16f);

        head.light(light)
            //			.color(color)
            .overlay(overlay).renderInto(ms, buffer.getBuffer(cutoutMipped));

        SuperByteBuffer tongue = CachedBuffers.partial(AllPartialModels.FROGPORT_TONGUE, blockEntity.getCachedState());

        tongue.center().rotateYDegrees(yaw).uncenter().translate(8 / 16f, 10 / 16f, 11 / 16f).rotateXDegrees(tonguePitch)
            .scale(1f, 1f, tongueLength / (7 / 16f)).translateBack(8 / 16f, 10 / 16f, 11 / 16f);

        tongue.light(light).overlay(overlay).renderInto(ms, buffer.getBuffer(cutoutMipped));

        // hat

        //		SuperByteBuffer hatBuffer = CachedBuffers.partial(AllPartialModels.TRAIN_HAT, blockEntity.getBlockState());
        //		hatBuffer
        //			.translate(8 / 16f, 14 / 16f, 8 / 16f)
        //			.rotateYDegrees(yaw + 180)
        //			.translate(0, 0, -3 / 16f)
        //			.rotateX(-4)
        //			.translateBack(0, 0, -3 / 16f)
        //			.translate(0, 0, 1 / 16f)
        //			.light(light)
        //			.color(color)
        //			.overlay(overlay)
        //			.renderInto(ms, buffer.getBuffer(RenderType.solid()));

    }

    private void renderPackage(
        FrogportBlockEntity blockEntity,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay,
        Vec3d diff,
        float scale,
        float itemDistance
    ) {
        if (blockEntity.animatedPackage == null)
            return;
        if (scale < 0.45)
            return;
        Identifier key = Registries.ITEM.getId(blockEntity.animatedPackage.getItem());
        if (key == Registries.ITEM.getDefaultId())
            return;
        SuperByteBuffer rigBuffer = CachedBuffers.partial(AllPartialModels.PACKAGE_RIGGING.get(key), blockEntity.getCachedState());
        SuperByteBuffer boxBuffer = CachedBuffers.partial(AllPartialModels.PACKAGES.get(key), blockEntity.getCachedState());

        boolean animating = blockEntity.isAnimationInProgress();
        boolean depositing = blockEntity.currentlyDepositing;

        for (SuperByteBuffer buf : new SuperByteBuffer[]{boxBuffer, rigBuffer}) {
            buf.translate(0, 3 / 16f, 0).translate(diff.normalize().multiply(itemDistance).subtract(0, animating && depositing ? 0.75 : 0, 0))
                .center().scale(scale).uncenter().light(light).overlay(overlay).renderInto(ms, buffer.getBuffer(RenderLayer.getCutout()));
            if (!blockEntity.currentlyDepositing)
                break;
        }
    }

}
