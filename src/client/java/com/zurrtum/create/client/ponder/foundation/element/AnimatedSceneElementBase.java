package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.ponder.api.element.AnimatedSceneElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class AnimatedSceneElementBase extends PonderElementBase implements AnimatedSceneElement {

    protected Vec3d fadeVec;
    protected LerpedFloat fade;

    public AnimatedSceneElementBase() {
        fade = LerpedFloat.linear().startWithValue(0);
    }

    @Override
    public void forceApplyFade(float fade) {
        this.fade.startWithValue(fade);
    }

    @Override
    public void setFade(float fade) {
        this.fade.setValue(fade);
    }

    @Override
    public void setFadeVec(Vec3d fadeVec) {
        this.fadeVec = fadeVec;
    }

    @Override
    public final void renderFirst(PonderLevel world, VertexConsumerProvider buffer, MatrixStack poseStack, float pt) {
        poseStack.push();
        float currentFade = applyFade(poseStack, pt);
        renderFirst(world, buffer, poseStack, currentFade, pt);
        poseStack.pop();
    }

    @Override
    public final void renderLayer(PonderLevel world, VertexConsumerProvider buffer, BlockRenderLayer type, MatrixStack poseStack, float pt) {
        poseStack.push();
        float currentFade = applyFade(poseStack, pt);
        renderLayer(world, buffer, type, poseStack, currentFade, pt);
        poseStack.pop();
    }

    @Override
    public final void renderLast(PonderLevel world, VertexConsumerProvider buffer, MatrixStack poseStack, float pt) {
        poseStack.push();
        float currentFade = applyFade(poseStack, pt);
        renderLast(world, buffer, poseStack, currentFade, pt);
        poseStack.pop();
    }

    protected float applyFade(MatrixStack ms, float pt) {
        float currentFade = fade.getValue(pt);
        if (fadeVec != null) {
            Vec3d scaled = fadeVec.multiply(-1 + currentFade);
            ms.translate(scaled.x, scaled.y, scaled.z);
        }

        return currentFade;
    }

    protected void renderLayer(PonderLevel world, VertexConsumerProvider buffer, BlockRenderLayer type, MatrixStack ms, float fade, float pt) {
    }

    protected void renderFirst(PonderLevel world, VertexConsumerProvider buffer, MatrixStack ms, float fade, float pt) {
    }

    protected void renderLast(PonderLevel world, VertexConsumerProvider buffer, MatrixStack ms, float fade, float pt) {
    }

    protected int lightCoordsFromFade(float fade) {
        int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        if (fade != 1) {
            light = (int) (MathHelper.lerp(fade, 5, 0xF));
            light = LightmapTextureManager.pack(light, light);
        }
        return light;
    }

}