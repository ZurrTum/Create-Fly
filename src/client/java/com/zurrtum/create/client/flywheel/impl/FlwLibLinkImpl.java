package com.zurrtum.create.client.flywheel.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.impl.compat.IrisCompat;
import com.zurrtum.create.client.flywheel.impl.extension.PoseStackExtension;
import com.zurrtum.create.client.flywheel.lib.internal.FlwLibLink;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

public class FlwLibLinkImpl implements FlwLibLink {
    @Override
    public Logger getLogger() {
        return FlwImpl.LOGGER;
    }

    @Override
    public PoseTransformStack getPoseTransformStackOf(PoseStack stack) {
        return ((PoseStackExtension) stack).flywheel$transformStack();
    }

    @Override
    public Map<String, ModelPart> getModelPartChildren(ModelPart part) {
        return part.children;
    }

    @Override
    public void compileModelPart(ModelPart part, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, int color) {
        part.compile(pose, consumer, light, overlay, color);
    }

    @Override
    public List<PoseStack.Pose> getPoseStack(PoseStack stack) {
        return stack.poses;
    }

    @Override
    public boolean isIrisLoaded() {
        return IrisCompat.ACTIVE;
    }

    @Override
    public boolean isOptifineInstalled() {
        //        return OptifineCompat.IS_INSTALLED;
        return false;
    }

    @Override
    public boolean isShaderPackInUse() {
        if (IrisCompat.ACTIVE) {
            return IrisCompat.isShaderPackInUse();
        } /*else if (OptifineCompat.IS_INSTALLED) {
            return OptifineCompat.isShaderPackInUse();
        }*/ else {
            return false;
        }
    }

    @Override
    public boolean isRenderingShadowPass() {
        if (IrisCompat.ACTIVE) {
            return IrisCompat.isRenderingShadowPass();
        } /*else if (OptifineCompat.IS_INSTALLED) {
            return OptifineCompat.isRenderingShadowPass();
        }*/ else {
            return false;
        }
    }
}
